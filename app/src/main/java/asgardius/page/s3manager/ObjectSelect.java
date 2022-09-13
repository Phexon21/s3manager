package asgardius.page.s3manager;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.S3ClientOptions;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

public class ObjectSelect extends AppCompatActivity {

    ArrayList Name;
    ArrayList Img;
    //ArrayList object;
    RecyclerView recyclerView;
    String username, password, endpoint, bucket, prefix, location;
    int treelevel;
    String[] filename;
    Region region;
    S3ClientOptions s3ClientOptions;
    AWSCredentials myCredentials;
    AmazonS3 s3client;
    ProgressBar simpleProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        endpoint = getIntent().getStringExtra("endpoint");
        username = getIntent().getStringExtra("username");
        password = getIntent().getStringExtra("password");
        bucket = getIntent().getStringExtra("bucket");
        location = getIntent().getStringExtra("region");
        prefix = getIntent().getStringExtra("prefix");
        treelevel = getIntent().getIntExtra("treelevel", 0);
        setContentView(R.layout.activity_object_select);
        region = Region.getRegion(location);
        s3ClientOptions = S3ClientOptions.builder().build();
        myCredentials = new BasicAWSCredentials(username, password);
        s3client = new AmazonS3Client(myCredentials, region);
        s3client.setEndpoint(endpoint);
        if (!endpoint.contains(getResources().getString(R.string.aws_endpoint))) {
            s3ClientOptions.setPathStyleAccess(true);
        }

        s3client.setS3ClientOptions(s3ClientOptions);


        recyclerView = findViewById(R.id.olist);
        simpleProgressBar = (ProgressBar) findViewById(R.id.simpleProgressBar);

        // layout for vertical orientation
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        Thread listobject = new Thread(new Runnable() {

            @Override
            public void run() {
                try  {
                    //Your code goes here
                    //List<Bucket> buckets = s3client.listBuckets();
                    ListObjectsRequest orequest = new ListObjectsRequest().withBucketName(bucket).withPrefix(prefix).withMaxKeys(8000);
                    //List<S3Object> objects = (List<S3Object>) s3client.listObjects(bucket, "/");
                    ObjectListing result = s3client.listObjects(orequest);
                    //System.out.println(objects);
                    //This convert bucket list to an array list
                    Img = new ArrayList<String>();
                    LinkedHashSet<String> object = new LinkedHashSet<String>();
                    // Print bucket names
                    //System.out.println("Buckets:");
                    //int i=0;
                    List<S3ObjectSummary> objects = result.getObjectSummaries();
                    boolean nextbatch = false;
                    while (result.isTruncated() || !nextbatch) {
                        if (nextbatch) {
                            result = s3client.listNextBatchOfObjects (result);
                            objects = result.getObjectSummaries();
                        } else {
                            nextbatch = true;
                        }
                        for (S3ObjectSummary os : objects) {
                            filename = os.getKey().split("/");
                            if (filename.length == treelevel+1) {
                                object.add(filename[treelevel]);
                            }
                            else {
                                object.add(filename[treelevel]+"/");
                            }

                            //i++;
                        }

                    }

                    Name = new ArrayList<String>(object);
                    object.clear();
                    //Img.add(R.drawable.unknownfile);
                    //This set object icon based on its filetype
                    int i = 0;
                    while(i<Name.size()) {
                        //Img.add(R.drawable.unknownfile);
                        if (Name.get(i).toString().endsWith("/")) {
                            Img.add(R.drawable.folder);
                        }
                        else if (Name.get(i).toString().endsWith(".txt") || Name.get(i).toString().endsWith(".md")) {
                            Img.add(R.drawable.textfile);
                        }
                        else if (Name.get(i).toString().endsWith(".jpg") || Name.get(i).toString().endsWith(".jpeg") || Name.get(i).toString().endsWith(".png") || Name.get(i).toString().endsWith(".gif")) {
                            Img.add(R.drawable.imagefile);
                        }
                        else if (Name.get(i).toString().endsWith(".opus") || Name.get(i).toString().endsWith(".ogg")
                                || Name.get(i).toString().endsWith(".oga") || Name.get(i).toString().endsWith(".mp3")
                                || Name.get(i).toString().endsWith(".m4a") || Name.get(i).toString().endsWith(".flac")
                                || Name.get(i).toString().endsWith(".mka")) {
                            Img.add(R.drawable.audiofile);
                        }
                        else if(Name.get(i).toString().endsWith(".mp4") || Name.get(i).toString().endsWith(".mkv")
                                || Name.get(i).toString().endsWith(".webm") || Name.get(i).toString().endsWith(".m4v")) {
                            Img.add(R.drawable.videofile);
                        }
                        else if (Name.get(i).toString().endsWith(".htm") || Name.get(i).toString().endsWith(".html")) {
                            Img.add(R.drawable.webpage);
                        }
                        else {
                            Img.add(R.drawable.unknownfile);
                        }
                        i++;
                    }



                    /*for (Bucket bucket : buckets) {
                        //i++;
                        //System.out.println(bucket.getName());
                        Name.add(bucket.getName());
                        //Img.add(R.drawable.ic_launcher_foreground);
                        Img.add(R.drawable.videofile);
                    }*/
                    //System.out.println(Name);

                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            // Sending reference and data to Adapter
                            Adapter adapter = new Adapter(ObjectSelect.this, Img, Name);
                            simpleProgressBar.setVisibility(View.INVISIBLE);

                            // Setting Adapter to RecyclerView
                            recyclerView.setAdapter(adapter);
                        }
                    });
                    //System.out.println("tree "+treelevel);
                    //System.out.println("prefix "+prefix);

                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),getResources().getString(R.string.media_list_fail), Toast.LENGTH_SHORT).show();
                        }
                    });
                    //Toast.makeText(getApplicationContext(),getResources().getString(R.string.media_list_fail), Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        });
        listobject.start();
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                //System.out.println("Click on "+Name.get(position).toString());
                //explorer(Name.get(position).toString());
                if (Img.get(position).equals(R.drawable.folder)) {
                    //go to subfolder
                    explorer(Name.get(position).toString());
                } else if (Img.get(position).equals(R.drawable.imagefile)) {
                    //load media file
                    try {
                        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucket, prefix + Name.get(position).toString());
                        URL objectURL = s3client.generatePresignedUrl(request);
                        imageViewer(objectURL.toString());
                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(),getResources().getString(R.string.media_list_fail), Toast.LENGTH_SHORT).show();
                    }
                } /*else if (Img.get(position).equals(R.drawable.textfile)) {
                    //load media file
                    try {
                        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucket, prefix + Name.get(position).toString());
                        URL objectURL = s3client.generatePresignedUrl(request);
                        textViewer(objectURL.toString());
                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(),getResources().getString(R.string.media_list_fail), Toast.LENGTH_SHORT).show();
                    }
                }*/ else if (Img.get(position).equals(R.drawable.webpage) || Img.get(position).equals(R.drawable.textfile)) {
                    //load media file
                    try {
                        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucket, prefix + Name.get(position).toString());
                        URL objectURL = s3client.generatePresignedUrl(request);
                        webBrowser(objectURL.toString());
                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(),getResources().getString(R.string.media_list_fail), Toast.LENGTH_SHORT).show();
                    }
                } else if (Img.get(position).equals(R.drawable.audiofile) || Img.get(position).equals(R.drawable.videofile)) {
                    //load media file
                    try {
                        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucket, prefix + Name.get(position).toString());
                        URL objectURL = s3client.generatePresignedUrl(request);
                        videoPlayer(objectURL.toString());
                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(),getResources().getString(R.string.media_list_fail), Toast.LENGTH_SHORT).show();
                    }
                }  else {
                    Toast.makeText(ObjectSelect.this, getResources().getString(R.string.unsupported_file), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onLongClick(View view, int position) {
                //System.out.println("Long click on "+Name.get(position).toString());
                if (Img.get(position).equals(R.drawable.folder)) {
                    //go to subfolder
                    /// Initializing the popup menu and giving the reference as current context
                    PopupMenu popupMenu = new PopupMenu(recyclerView.getContext(), view);

                    // Inflating popup menu from popup_menu.xml file
                    popupMenu.getMenuInflater().inflate(R.menu.folder_menu, popupMenu.getMenu());
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            // Toast message on menu item clicked
                            //Toast.makeText(MainActivity.this, "You Clicked " + menuItem.getTitle(), Toast.LENGTH_SHORT).show();
                            if (menuItem.getTitle() == getResources().getString(R.string.upload_file_here)) {
                                Toast.makeText(ObjectSelect.this, getResources().getString(R.string.pending_feature), Toast.LENGTH_SHORT).show();
                                //upload(false);
                            } else if (menuItem.getTitle() == getResources().getString(R.string.file_del)) {
                                if (Name.size() == 1 && treelevel >= 1) {
                                    Toast.makeText(ObjectSelect.this, getResources().getString(R.string.only_item_onlist), Toast.LENGTH_SHORT).show();
                                } else {
                                    delete(prefix + Name.get(position).toString(), true);
                                }
                            }
                            return true;
                        }
                    });
                    // Showing the popup menu
                    popupMenu.show();
                } else {
                    // Initializing the popup menu and giving the reference as current context
                    PopupMenu popupMenu = new PopupMenu(recyclerView.getContext(), view);

                    // Inflating popup menu from popup_menu.xml file
                    popupMenu.getMenuInflater().inflate(R.menu.object_menu, popupMenu.getMenu());
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            // Toast message on menu item clicked
                            //Toast.makeText(MainActivity.this, "You Clicked " + menuItem.getTitle(), Toast.LENGTH_SHORT).show();
                            if (menuItem.getTitle() == getResources().getString(R.string.upload_file_here)) {
                                Toast.makeText(ObjectSelect.this, getResources().getString(R.string.pending_feature), Toast.LENGTH_SHORT).show();
                                //upload(false);
                            } else if (menuItem.getTitle() == getResources().getString(R.string.file_external)) {
                                try {
                                    GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucket, prefix + Name.get(position).toString());
                                    URL objectURL = s3client.generatePresignedUrl(request);
                                    share(objectURL.toString());
                                } catch (Exception e) {
                                    Toast.makeText(getApplicationContext(),getResources().getString(R.string.media_list_fail), Toast.LENGTH_SHORT).show();
                                }
                            } else if (menuItem.getTitle() == getResources().getString(R.string.file_del)) {
                                if (menuItem.getTitle() == getResources().getString(R.string.file_del)) {
                                    if (Name.size() == 1 && treelevel >= 1) {
                                        Toast.makeText(ObjectSelect.this, getResources().getString(R.string.only_item_onlist), Toast.LENGTH_SHORT).show();
                                    } else {
                                        delete(prefix + Name.get(position).toString(), false);
                                    }
                                }
                            }
                            return true;
                        }
                    });
                    // Showing the popup menu
                    popupMenu.show();
                }
            }
        }));
    }

    private void videoPlayer(String url) {

        Intent intent = new Intent(this, VideoPlayer.class);
        intent.putExtra("video_url", url);
        startActivity(intent);

    }
    private void textViewer(String url) {

        Intent intent = new Intent(this, TextViewer.class);
        intent.putExtra("video_url", url);
        startActivity(intent);

    }

    private void imageViewer(String url) {

        Intent intent = new Intent(this, ImageViewer.class);
        intent.putExtra("video_url", url);
        startActivity(intent);

    }

    private void webBrowser(String url) {

        Intent intent = new Intent(this, WebBrowser.class);
        intent.putExtra("web_url", url);
        startActivity(intent);

    }

    private void explorer(String object) {

        Intent intent = new Intent(this, ObjectSelect.class);
        //treelevel ++;
        intent.putExtra("endpoint", endpoint);
        intent.putExtra("username", username);
        intent.putExtra("password", password);
        intent.putExtra("bucket", bucket);
        intent.putExtra("prefix", prefix + object);
        intent.putExtra("treelevel", treelevel+1);
        intent.putExtra("region", location);
        startActivity(intent);

    }

    private void share(String object) {

        try {

            Intent shareIntent = new Intent(Intent.ACTION_VIEW);
            shareIntent.setData(Uri.parse(object));
            startActivity(Intent.createChooser(shareIntent, "choose one"));
        } catch(Exception e) {
            Toast.makeText(getApplicationContext(),getResources().getString(R.string.media_list_fail), Toast.LENGTH_SHORT).show();
        }

    }

    private void delete(String object, boolean folder) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ObjectSelect.this);
        builder.setCancelable(true);
        builder.setTitle(getResources().getString(R.string.file_del));
        if (folder) {
            builder.setMessage(getResources().getString(R.string.folder_del_confirm));
        } else {
            builder.setMessage(getResources().getString(R.string.file_del_confirm));
        }
        builder.setPositiveButton(android.R.string.ok,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Toast.makeText(ObjectSelect.this, getResources().getString(R.string.pending_feature), Toast.LENGTH_SHORT).show();
                        Thread deleteObject = new Thread(new Runnable() {

                            @Override
                            public void run() {
                                try  {
                                    //Your code goes here
                                    //List<Bucket> buckets = s3client.listBuckets();
                                    if (folder) {
                                        ListObjectsRequest orequest = new ListObjectsRequest().withBucketName(bucket).withPrefix(object).withMaxKeys(8000);
                                        //List<S3Object> objects = (List<S3Object>) s3client.listObjects(bucket, "/");
                                        ObjectListing result = s3client.listObjects(orequest);
                                        ArrayList<String> objectl = new ArrayList<String>();
                                        List<S3ObjectSummary> objects = result.getObjectSummaries();
                                        boolean nextbatch = false;
                                        while (result.isTruncated() || !nextbatch) {
                                            if (nextbatch) {
                                                result = s3client.listNextBatchOfObjects (result);
                                                objects = result.getObjectSummaries();
                                            } else {
                                                nextbatch = true;
                                            }
                                            for (S3ObjectSummary os : objects) {
                                                objectl.add(os.getKey());

                                                //i++;
                                            }

                                        }
                                        //System.out.println(object);
                                        DeleteObjectsRequest deleteObjectsRequest = new DeleteObjectsRequest(bucket).withKeys(objectl.toArray(new String[0]));
                                        s3client.deleteObjects(deleteObjectsRequest);

                                    } else {
                                        DeleteObjectRequest deleteObjectRequest = new DeleteObjectRequest(bucket, object);
                                        s3client.deleteObject(deleteObjectRequest);
                                    }

                                                                        //System.out.println(Name);

                                    runOnUiThread(new Runnable() {

                                        @Override
                                        public void run() {
                                            // Sending reference and data to Adapter
                                            if (folder) {
                                                Toast.makeText(getApplicationContext(),getResources().getString(R.string.folder_del_success), Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(getApplicationContext(),getResources().getString(R.string.file_del_success), Toast.LENGTH_SHORT).show();
                                            }
                                            recreate();

                                        }
                                    });
                                    //System.out.println("tree "+treelevel);
                                    //System.out.println("prefix "+prefix);

                                } catch (Exception e) {
                                    e.printStackTrace();
                                    runOnUiThread(new Runnable() {

                                        @Override
                                        public void run() {
                                            Toast.makeText(getApplicationContext(),getResources().getString(R.string.media_list_fail), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                    //Toast.makeText(getApplicationContext(),getResources().getString(R.string.media_list_fail), Toast.LENGTH_SHORT).show();
                                    finish();
                                }
                            }
                        });
                        simpleProgressBar.setVisibility(View.VISIBLE);
                        deleteObject.start();
                    }
                });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void upload(boolean isfolder) {
        Intent intent = new Intent(this, Uploader.class);
        intent.putExtra("endpoint", endpoint);
        intent.putExtra("username", username);
        intent.putExtra("password", password);
        intent.putExtra("bucket", bucket);
        intent.putExtra("prefix", prefix);
        intent.putExtra("region", location);
        intent.putExtra("isfolder", isfolder);
        startActivity(intent);
    }
}