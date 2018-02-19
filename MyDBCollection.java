package jp.enpit.cloud.mongo;

import java.util.List;
import java.util.Iterator;

import com.mongodb.AggregationOutput;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

public class MyDBCollection {

	private DBCollection coll;
	private String name, eventName, seatName, userId;
	private int count;
	private int turn;
	private Object lock = new Object();
	//private Queue queue;

	public MyDBCollection(DBCollection collection, String th,String ev, String se, String Id, int ct, int flag) {
		// TODO 自動生成されたコンストラクター・スタブ
		coll = collection;
		name = th;
		eventName = ev;
		seatName = se;
		userId = Id;
		count = ct;
		this.turn = flag;//flag == 0;
	}

	public AggregationOutput aggregate(List<DBObject> pipeline) {
		// TODO 自動生成されたメソッド・スタブ
		return coll.aggregate(pipeline);
	}

	public void update(DBObject bquery, DBObject pending) {

		try {
			if(this.name == "main"){
				//Thread.sleep(1000);
				Thread.sleep(1000);
				coll.update(bquery, pending);
				System.out.println("update by "+this.name);
			}else/*if(this.name == "threadA")*/{
				Thread.sleep(1000);
				coll.update(bquery, pending);
				System.out.println("update by "+this.name);
			}
		} catch (InterruptedException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			}
		//}catch (InterruptedException e) {
			//String msg = "Sold out";
			//throw new TicketSoldOutException(msg);
		//}

	}

	public DBCursor find(DBObject pquery) {
		// TODO 自動生成されたメソッド・スタブ
		return coll.find(pquery);
	}

	public void updateMulti(DBObject pquery, DBObject status) {
		// TODO 自動生成されたメソッド・スタブ
		coll.updateMulti(pquery, status);//本予約
	}

}
