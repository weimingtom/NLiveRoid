package nliveroid.nlr.main.parser;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nliveroid.nlr.main.ErrorCode;
import nliveroid.nlr.main.LiveInfo;
import nliveroid.nlr.main.SearchTab.SearchTask;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import android.graphics.Bitmap;

public class TimeTableParser implements ContentHandler {

	private String startTag;
	private Attributes nowAttr;
	private StringBuilder innerText = new StringBuilder(1024);
	private ArrayList<LiveInfo> liveInfos = new ArrayList<LiveInfo>();
	private ArrayList<Bitmap> thumbNails = new ArrayList<Bitmap>();
	private LinkedHashMap<String,String> generate = new LinkedHashMap<String,String>();
	private LiveInfo tempInfo;
	private int spanCount = 0;
	private boolean generateArea = false;
	private SearchTask task;
	private Pattern lvpt = Pattern.compile("lv[0-9]+");
	private Pattern copt = Pattern.compile("co[0-9]+");
	private Pattern chpt = Pattern.compile("ch[0-9]+");
	private ErrorCode error;//最初のパースとサムネイル取得に必要
	public TimeTableParser(SearchTask task,ErrorCode error){
		this.task = task;
		this.error = error;
	}


	private String getInnerText(char[] arg0,int arg2){
		innerText = innerText.delete(0,arg0.length);
		innerText.append(arg0, 0, arg2);
		return innerText.toString();
	}

	@Override
	public void characters(char[] arg0, int arg1, int arg2) throws SAXException {
		if(nowAttr != null && nowAttr.getLength() > 0){
			if(startTag.equals("div")){
				String attrValue = nowAttr.getValue(0);
//				Log.d("Log","DIV " + attrValue);
				if(attrValue.equals("watch_area")){
					tempInfo = new LiveInfo();//カテゴリ側はここでnew
					Matcher mc = null;
						mc = lvpt.matcher(nowAttr.getValue(1));
						if(mc.find()){
//							Log.d("Log","SETCATEGORY LV" + mc.group());
							tempInfo.setLiveID(mc.group());
						}
				}else if(attrValue.equals("gate_button") && tempInfo.getLiveID() != null){
//					Log.d("SearchParser","ADD CATEGORY INFO");
					liveInfos.add(tempInfo.clone());
					tempInfo.setLiveID(null);
				}else if(attrValue.equals("icon official")&&!tempInfo.getTags().contains("official")){
					tempInfo.setTags( tempInfo.getTags() + "<<TAGXXX>>official");
				}else if(attrValue.equals("icon common")&&!tempInfo.getTags().contains("common")){
					tempInfo.setTags( tempInfo.getTags() + "<<TAGXXX>>common");
				}else if(attrValue.equals("icon only")&&!tempInfo.getTags().contains("only")){
					tempInfo.setTags( tempInfo.getTags() + "<<TAGXXX>>only");
				}else if(attrValue.equals("icon face")&&!tempInfo.getTags().contains("face")){
					tempInfo.setTags( tempInfo.getTags() + "<<TAGXXX>>face");
				}else if(attrValue.equals("icon totu")&&!tempInfo.getTags().contains("totu")){
					tempInfo.setTags( tempInfo.getTags() + "<<TAGXXX>>totu");
				}else if(attrValue.equals("icon live")&&!tempInfo.getTags().contains("live")){
					tempInfo.setTags( tempInfo.getTags() + "<<TAGXXX>>live");
				}else if(attrValue.equals("icon play")&&!tempInfo.getTags().contains("play")){
					tempInfo.setTags( tempInfo.getTags() + "<<TAGXXX>>play");
				}else if(attrValue.equals("icon sing")&&!tempInfo.getTags().contains("sing")){
					tempInfo.setTags( tempInfo.getTags() + "<<TAGXXX>>sing");
				}else if(attrValue.equals("icon lecture")&&!tempInfo.getTags().contains("lecture")){
					tempInfo.setTags( tempInfo.getTags() + "<<TAGXXX>>lecture");
				}else if(attrValue.equals("icon request")&&!tempInfo.getTags().contains("request")){
					tempInfo.setTags( tempInfo.getTags() + "<<TAGXXX>>request");
				}else if(attrValue.equals("icon channel")&&!tempInfo.getTags().contains("channel")){
					tempInfo.setTags( tempInfo.getTags() + "<<TAGXXX>>channel");
				}else if(attrValue.equals("icon draw")&&!tempInfo.getTags().contains("draw")){
					tempInfo.setTags( tempInfo.getTags() + "<<TAGXXX>>draw");
				}else if(attrValue.equals("icon politics")&&!tempInfo.getTags().contains("politics")){
					tempInfo.setTags( tempInfo.getTags() + "<<TAGXXX>>politics");
				}else if(attrValue.equals("start")){//開始時間
					String startTime = getInnerText(arg0,arg2).replaceAll(" |\t|　|\n", "");
//					Log.d("STARTTIE"," ---- " + startTime);
					if(startTime.matches(".+")){
//						Log.d("MATCHSTART","   " + startTime);
					tempInfo.setStartTime(startTime);
					}
				}else if(tempInfo != null && attrValue.equals("title")){//<div class="title">種類</div>とかいうのがHTMLの上のほうにあるからtempInfo!=null
					String title = getInnerText(arg0, arg2);
//					Log.d("TITILE===","TITI" + title);
						title.replaceAll("\t| |　|\n", "");
					if(title.matches(".+")){//文字がない場合がある
					tempInfo.setTitle(title);
					}
				}
			}else if(startTag.equals("h2")&&tempInfo != null && tempInfo.getLiveID() != null&&tempInfo.getTitle().equals("")){//キーワード・タグ側のタイトル
//					Log.d("H2","ATTR " + nowAttr.getValue(0));//charactersなので何回もtitle属性が来る
					if(nowAttr.getValue(0).equals("title")){
						String inn = getInnerText(arg0,arg2);
//						Log.d("INNER","  " + inn);
						tempInfo.setTitle(getInnerText(arg0,arg2));
					}
			}else if(startTag.equals("p")&&tempInfo != null && tempInfo.getLiveID() != null && tempInfo.getStartTime().equals("-")){//キーワード・タグ側の開始時間
//				Log.d("p","---");
//					Log.d("p","ATTR " + nowAttr.getValue(0));
					if(nowAttr.getValue(0).equals("desc")){
						String inn = getInnerText(arg0,arg2);
//						Log.d("DESC","  " + inn);
						tempInfo.setStartTime(getInnerText(arg0,arg2));
					}
			}else if(startTag.equals("span")&&tempInfo != null && tempInfo.getLiveID() != null){//キーワード・タグ側の来場者
				spanCount++;
//				Log.d("span","ATTR " + nowAttr.getValue(0));
				switch(spanCount){
				case 4:
					tempInfo.setViewCount(getInnerText(arg0,arg2));
					break;
				case 6:
					tempInfo.setResNumber(getInnerText(arg0,arg2));
					break;
				case 8:
					tempInfo.setReservedcount(getInnerText(arg0,arg2));
					break;
				}
			}else if(startTag.equals("img")){
				String attrValue = null;
				for(int i = 0; i < nowAttr.getLength(); i++){
				attrValue = nowAttr.getValue(i);
					if(attrValue.equals("img/smartphone/view.png")){
						//カンマ入ってくる
						Matcher decimal = Pattern.compile("[0-9]{1,3},[0-9]{3},[0-9]{3}|[0-9]{1,3},[0-9]{3}|[0-9]{1,3}").matcher(getInnerText(arg0,arg2));
						if(decimal.find()){
//							Log.d("log","VIEWCOUNT " + decimal.group());
							tempInfo.setViewCount(decimal.group());
						}
					}else if(attrValue.equals("img/smartphone/comment.png")){
						Matcher decimal = Pattern.compile("[0-9]{1,3},[0-9]{3},[0-9]{3}|[0-9]{1,3},[0-9]{3}|[0-9]{1,3}").matcher(getInnerText(arg0,arg2));
						if(decimal.find()){
//							Log.d("log","COMCOUNT " + decimal.group());
							tempInfo.setResNumber(decimal.group());
						}
					}else if(attrValue.contains("http://icon.nimg")){//ユーザーとチャンネル IDを元にサムネイル取得
						Matcher comc = copt.matcher(attrValue);
						if(comc.find()){
							//ここがchar...だから複数回呼ばれるので、co2回呼ばれるけどめんどうなので2回セットでいい
//							Log.d("CO","MATCH" + comc.group());
							tempInfo.setCommunityID(comc.group());
						}else{//coじゃなければチャンネルとする
							Matcher chmc = chpt.matcher(attrValue);
							if(chmc.find()){
							//チャンネルはサムネURLでなくIDで統一
							tempInfo.setCommunityID(chmc.group());
							}
						}
					}else{
						Matcher thumb = Pattern.compile("thumb/[0-9]+\\..*\\.jpg|thumb/[0-9]+\\..*\\.png").matcher(attrValue);
						if(thumb.find()){
							//公式のサムネ番号はサムネURLに入れちゃう
						tempInfo.setThumbnailURL(thumb.group());
						}
					}
				}
			}else if(generateArea && startTag.equals("a") && nowAttr != null && nowAttr.getValue("href") != null){
				String inner = getInnerText(arg0,arg2);
						if(inner == null)return;
				inner = inner.replaceAll("\t|\n", "");
				if(inner.equals(""))return;
				generate.put(inner, nowAttr.getValue("href"));
			}
		}

	}

	@Override
	public void startElement(String arg0, String arg1, String arg2,
			Attributes arg3) throws SAXException {
		startTag=arg1;
		nowAttr = arg3;
		if(arg1.equals("ul") && arg3 != null && arg3.getValue("class") != null && arg3.getValue("class").equals("listIPhone listTypeA")){
			generateArea = true;
		}
	}

	@Override
	public void endDocument() throws SAXException {}
	@Override
	public void endElement(String arg0, String arg1, String arg2)throws SAXException {
//		Log.d("Log","END ELEMENT " + arg1);
		if(arg1.equals("ul"))generateArea = false;

		if(arg1.equals("body")){
			task.finishCallBack(liveInfos,generate);
		}
	}
	@Override
	public void endPrefixMapping(String arg0) throws SAXException {}
	@Override
	public void ignorableWhitespace(char[] arg0, int arg1, int arg2)
			throws SAXException {
		// TODO
	}

	@Override
	public void processingInstruction(String arg0, String arg1)
			throws SAXException {
		// TODO
	}
	@Override
	public void setDocumentLocator(Locator arg0) {
		// TODO

	}

	@Override
	public void skippedEntity(String arg0) throws SAXException {
		// TODO

	}

	@Override
	public void startDocument() throws SAXException {
		// TODO

	}


	@Override
	public void startPrefixMapping(String arg0, String arg1)
			throws SAXException {
		// TODO

	}




}
