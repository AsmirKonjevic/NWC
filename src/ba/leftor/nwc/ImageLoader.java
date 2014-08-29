package ba.leftor.nwc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.SoftReference;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

public class ImageLoader {
    
    MemoryCache memoryCache=new MemoryCache();
    FileCache fileCache;
    private Map<ImageView, String> imageViews=Collections.synchronizedMap(new WeakHashMap<ImageView, String>());
    ExecutorService executorService; 
    
    public ImageLoader(Context context){
        fileCache=new FileCache(context);
        executorService=Executors.newFixedThreadPool(5);
    }
    
    final int stub_id=R.drawable.ic_launcher;
    public void DisplayImage(String url, ImageView imageView,Boolean velika)
    {
    	if(velika){
    		Log.w("IMAGELOADER","VELIKA SLIKA LOAD");
    	} 
    	
    	
    	
    	URL url1=null;
		try {
			url1 = new URL(url);
	    	URI uri = new URI(url1.getProtocol(), url1.getUserInfo(), url1.getHost(), url1.getPort(), url1.getPath(), url1.getQuery(), url1.getRef());
	    	url1 = uri.toURL();
		} catch (Exception e) {
		}
		try {
			url=url1.toString();
		} catch (Exception e) {
		}
    	
    	imageViews.put(imageView, url);
        Bitmap bitmap=memoryCache.get(url);
        if(bitmap!=null){
            imageView.setImageBitmap(bitmap);
        	imageView.setVisibility(View.VISIBLE);
        }else
        {
            queuePhoto(url, imageView);
            imageView.setImageResource(stub_id);
            imageView.setVisibility(View.INVISIBLE);
        }
    }
        
    private void queuePhoto(String url, ImageView imageView)
    {
        PhotoToLoad p=new PhotoToLoad(url, imageView);
        executorService.submit(new PhotosLoader(p));
    }
    
    private Bitmap getBitmap(String url) 
    {
        File f=fileCache.getFile(url);
        
        //from SD cache
        Bitmap b = decodeFile(f);
        if(b!=null)
            return b;
        
        //from web
        try {
            Bitmap bitmap=null;
            URL imageUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection)imageUrl.openConnection();
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(30000);
            conn.setInstanceFollowRedirects(true);
            InputStream is=conn.getInputStream();
            OutputStream os = new FileOutputStream(f);
            GlobalFunctionsAndConstants.CopyStream(is, os);
            os.close();
            bitmap = decodeFile(f);
            return bitmap;
        } catch (Exception ex){
           ex.printStackTrace();
           return null;
        }
    }

    //decodes image and scales it to reduce memory consumption
    private Bitmap decodeFile(File f){
        try {
            //decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(f),null,o);
            
            //Find the correct scale value. It should be the power of 2.
            final int REQUIRED_SIZE=128;
            int width_tmp=o.outWidth, height_tmp=o.outHeight;
            int scale=1;
            while(true){
                if(width_tmp/2<REQUIRED_SIZE || height_tmp/2<REQUIRED_SIZE)
                    break;
                width_tmp/=2;
                height_tmp/=2;
                scale*=2;
            }
            
            //decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize=scale;
            return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
        } catch (FileNotFoundException e) {}
        return null;
    }
    
    //Task for the queue
    private class PhotoToLoad
    {
        public String url;
        public ImageView imageView;
        public PhotoToLoad(String u, ImageView i){
            url=u; 
            imageView=i;
        }
    }
    
    class PhotosLoader implements Runnable {
        PhotoToLoad photoToLoad;
        PhotosLoader(PhotoToLoad photoToLoad){
            this.photoToLoad=photoToLoad;
        }
        
        //@Override
        public void run() {
            if(imageViewReused(photoToLoad))
                return;
            Bitmap bmp=getBitmap(photoToLoad.url);
            memoryCache.put(photoToLoad.url, bmp);
            if(imageViewReused(photoToLoad))
                return;
            BitmapDisplayer bd=new BitmapDisplayer(bmp, photoToLoad);
            Activity a=(Activity)photoToLoad.imageView.getContext();
            a.runOnUiThread(bd);
        }
    }
    
    boolean imageViewReused(PhotoToLoad photoToLoad){
        String tag=imageViews.get(photoToLoad.imageView);
        if(tag==null || !tag.equals(photoToLoad.url))
            return true;
        return false;
    }
    
    //Used to display bitmap in the UI thread
    class BitmapDisplayer implements Runnable
    {
        Bitmap bitmap;
        PhotoToLoad photoToLoad;
        public BitmapDisplayer(Bitmap b, PhotoToLoad p){bitmap=b;photoToLoad=p;}
        public void run()
        {
            if(imageViewReused(photoToLoad))
                return;
            if(bitmap!=null){
                photoToLoad.imageView.setImageBitmap(bitmap);
                photoToLoad.imageView.setVisibility(View.VISIBLE);
            
            }else{
                photoToLoad.imageView.setImageResource(stub_id);
                photoToLoad.imageView.setVisibility(View.INVISIBLE);
            }
        }
    }
    
    /**
     * Return the size of a directory in bytes
     */
    public long dirSize() {
        return fileCache.dirSize(); // return the file size
    }

    public void clearCache() {
        memoryCache.clear();
        fileCache.clear();
    }
    
    
    ////ke�iranje u memoriji
    private class MemoryCache {
        private HashMap<String, SoftReference<Bitmap>> cache=new HashMap<String, SoftReference<Bitmap>>();
        
        public Bitmap get(String id){
            if(!cache.containsKey(id))
                return null;
            SoftReference<Bitmap> ref=cache.get(id);
            return ref.get();
        }
        
        public void put(String id, Bitmap bitmap){
            cache.put(id, new SoftReference<Bitmap>(bitmap));
        }

        public void clear() {
            cache.clear();
        }
    }
    
    ///klasa za ke�iranje u file (SD kartica)
    private class FileCache {
        
        private File cacheDir;
        
        public FileCache(Context context){
            //Find the dir to save cached images
            if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))
                cacheDir=new File(android.os.Environment.getExternalStorageDirectory(),"NwcCache");
            else
                cacheDir=context.getCacheDir();
            if(!cacheDir.exists())
                cacheDir.mkdirs();
        }
        
        public File getFile(String url){
            //I identify images by hashcode. Not a perfect solution, good for the demo.
            String filename=String.valueOf(url.hashCode());
            //Another possible solution (thanks to grantland)
            //String filename = URLEncoder.encode(url);
            File f = new File(cacheDir, filename);
            return f;
            
        }
        
        public void clear(){
            File[] files=cacheDir.listFiles();
            for(File f:files)
                f.delete();
        }
        
        /**
         * Return the size of a directory in bytes
         */
        public long dirSize() {
            long result = 0;
            File[] fileList = cacheDir.listFiles();

            for(int i = 0; i < fileList.length; i++) {
                // Recursive call if it's a directory
                
                // Sum the file size in bytes
                result += fileList[i].length();
                
            }
            return result; // return the file size
        }

    }


}

