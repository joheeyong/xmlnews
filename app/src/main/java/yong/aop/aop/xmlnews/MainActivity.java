package yong.aop.aop.xmlnews;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import yong.aop.aop.xmlnews.databinding.ItemNewsBinding;

public class MainActivity extends ListActivity {


    private OkHttpClient mClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);

        mClient = new OkHttpClient ();

        new Thread (new Runnable () {
            @Override
            public void run() {
                try{
                    Request request =  new Request.Builder ()
                            .url ("https://news.google.com/rss?hl=ko&gl=KR&ceid=KR:ko")
                            .build ();

                    Response response;
                    response = mClient.newCall(request).execute ();
                    String xml = response.body ().string ();

                    final List<News> data = parse(xml);
                    runOnUiThread (() -> {
                        NewsAdapater adapater = new NewsAdapater (data);
                        setListAdapter (adapater);
                    });
                } catch (IOException e) {
                    e.printStackTrace ();
                }
            }
        }).start ();

    }

    private static class NewsAdapater extends BaseAdapter {
        ItemNewsBinding mmbinding;
        private List<News> mData;
//        private AdapterView.OnItemClickListener listener;
        private Context context;

//        public void setOnItemClickListener(AdapterView.OnItemClickListener listener) {
//            this.listener = listener;
//        }
//        public interface OnItemClickListener {
//            void onItemClick();
//        }


        public NewsAdapater(List<News> data) { mData = data;
            this.context = context;}

        @Override
        public int getCount() {
            return mData.size ();
        }

        @Override
        public Object getItem(int position) {
            return mData.get (position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if(convertView == null) {
                mmbinding = DataBindingUtil.inflate (LayoutInflater.from (parent.getContext ()),
                        R.layout.item_news, parent, false);

                convertView = mmbinding.getRoot ();

                holder =  new ViewHolder (convertView);

                holder.titleTextView =   mmbinding.titleText;
                holder.dateTextView = mmbinding.titleText2;
                holder.dateTextView3 = mmbinding.titleText3;
                convertView.setTag(holder);
            } else{
                holder =  (ViewHolder) convertView.getTag ();
            }

            News news =  (News) getItem (position);
            holder.titleTextView.setText (news.title);
            holder.dateTextView.setText (news.pubDate);
            holder.dateTextView3.setText (news.link);

                return convertView;
        }
    }

    private static class ViewHolder {

        ViewHolder(View itemView){
            super();

            itemView.setOnClickListener (new View.OnClickListener () {
                @Override
                public void onClick(View v) {
                    Context context = v.getContext();
                    Intent intent = new Intent(v.getContext(),two.class);
                    intent.putExtra ("abc",dateTextView3.getText ());
                    context.startActivity(intent);
                }

            });
        }
        TextView titleTextView;
        TextView dateTextView;
        TextView dateTextView3;




    }


    private List<News> parse(String xml) {
        try{
            return new NewsParser ().parse(xml);
        } catch (XmlPullParserException e) {
            e.printStackTrace ();
        } catch (IOException e) {
            e.printStackTrace ();
        }
        return null;
    }

    private static class News {
        String title;
        String link;
        String pubDate;
        String category;

        @Override
        public String toString() {
            return "News{" +
                    "title='" + title + '\'' +
                    ", link='" + link + '\'' +
                    ", pubDate='" + pubDate + '\'' +
                    ", category='" + category + '\'' +
                    '}';
        }
    }

    private static class  NewsParser {
        public List<News> parse(String xml) throws XmlPullParserException, IOException {

            List<News> newsList =  new ArrayList<>();
            News news = null;
            String text= "";
            boolean isItem = false;

            XmlPullParser parser = Xml.newPullParser ();

            parser.setFeature (XmlPullParser.FEATURE_PROCESS_NAMESPACES,false);
            parser.setInput (new StringReader(xml));
            int eventType =  parser.getEventType ();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                String tagName = parser.getName ();

                switch (eventType){
                    case XmlPullParser.START_TAG:
                        if(tagName.equals ("item")){
                            news = new News();
                            isItem=true;
                        }
                        break;

                    case XmlPullParser.TEXT:
                        if (isItem){
                            text = parser.getText ();
                        }
                        break;

                    case XmlPullParser.END_TAG:
                        if (isItem) {
                            if (tagName.equals ("item")) {
                                newsList.add (news);
                                isItem = false;
                            } else if (tagName.equals ("title")) {
                                news.title = text;
                            } else if (tagName.equals ("link")) {
                                    news.link = text;
                                } else if (tagName.equals ("category")) {
                                    news.category = text;
                                } else if (tagName.equals ("pubDate")) {
                                    news.pubDate = text;
                                }
                            }
                            break;
                    default:
                }
                eventType= parser.next ();
            }
            return newsList;
        }
    }
}