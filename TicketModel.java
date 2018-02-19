package jp.enpit.cloud.mongo;

import java.net.UnknownHostException;//java.net:ネットワーク通信を行うためのクラス
import java.util.ArrayList;//java.util:プログラミングを便利にする様々なクラス
import java.util.List;//List生成に必要
//c:\pbl\java\src\java内のデータ
import com.mongodb.AggregationOutput;//以下、MongoDBの機能
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoException;
//c:\pbl\mongodb内のデータ
public class TicketModel {

	private MyDBCollection coll = null;

	public TicketModel(String threadName, String eventName, String seatName, String userId, int count, int flag) {//コンストラクタ,データベースの呼び出し
		try {
			MyMongoClient mongo =  new MyMongoClient("localhost", 27017);//mongoDBに接続
			MyDB db = mongo.getDB("tem");//データベース名tem
			coll = db.getCollection("ticket", threadName, eventName, seatName, userId, count, flag);//使用するコレクションの指定
		} catch (UnknownHostException e) {
			// Exception Handling
		}
	}


	/**
	 * 演習3: イベントごとの空席数を取得する
	 * @return イベント情報（イベント名と空席数）のリスト
	 */
	public List<EventInfo> getVacantSeatNumList() {
		List<EventInfo> events = new ArrayList<EventInfo>();//Listの生成

		try {
			// "status（現状）"が"blank（空き）"のチケットについて"eventName"ごとにカウントする
			//コマンド入力
			// db.ticket.aggregate(
			//   [
			//     {"$match" : {"status" : "blank"}},

			//     {"$group" : {"_id" : {"eventName" : "$eventName"}, "num" : {"$sum" : 1}}}
			//   ]
			// )
			DBObject match = new BasicDBObject("$match",
					new BasicDBObject("status", "blank"));

			DBObject group = new BasicDBObject("$group",
					new BasicDBObject("_id",
							new BasicDBObject("eventName", "$eventName")
					).append("num", new BasicDBObject("$sum", 1)));

			List<DBObject> pipeline = new ArrayList<DBObject>();
			pipeline.add(match);//Listへのデータの追加:リストの名前.add(データ);
			pipeline.add(group);

			AggregationOutput aggr = coll.aggregate(pipeline);//pipelineリスト内の集計

			// aggregateの結果（results()）からすべてのeventNameとcountを取得して
			// EventInfoを作成し、events（イベント情報）に追加（add()）する
			for (DBObject o: aggr.results()) {
				EventInfo e = new EventInfo();//EventInfo型のインスタンスeの生成
				DBObject id = (DBObject) o.get("_id");
				e.setEventName((String) id.get("eventName"));
				e.setVacantSeatNum((int) o.get("num"));
				events.add(e);
			}//イベント情報の更新
		} catch (MongoException e) {
		}

		return events;
	}

	/**
	 * 演習4: チケットを購入する
	 * @param eventName イベント名//引数と引数の概要に関しての記述
	 * @param seatName シート名
	 * @param userId チケット購入者
	 * @param count チケット購入枚数
	 * @throws TicketSoldOutException チケットが必要枚数確保できなかった場合//メソッドが投げる例外クラスとその概要を記述
	 */
	public void buyTickets(String eventName, String seatName, String userId, int count) throws TicketSoldOutException {
		try {
			//1フェーズ目
			//コマンド入力
			//db.ticket.update(
			//     {"eventName" : eventName, "seatName" : seatName, "status" : "blank"},
			//     {"$set" : {"owner" : userId, "status" : "pending(保留中)"}}
			// )
			// をcount回実施し、必要枚数を仮押さえする
			DBObject bquery = new BasicDBObject("eventName", eventName)
										.append("seatName", seatName)
										.append("status", "blank");//空きである指定されたチケット

			DBObject pending = new BasicDBObject("$set",
					new BasicDBObject("owner", userId).append("status", "pending"));//仮押さえ処理の内容

			for (int i = 0; i < count; i++) {//空きの分が要求分より少ないと？？
				coll.update(bquery, pending);
			}//count枚数分の仮押さえ（blankをpendingに書き換え）　このメソッドでのpending要求を全てstubで制御


			//コマンド入力
			// db.ticket.find(
			//     {"eventName" : eventName, "seatName" : seatName, "owner" : userId, "status" : "pending"},
			// ).count()
			// により必要枚数が確保できていることを確認する
			DBObject pquery = new BasicDBObject("eventName", eventName)
										.append("seatName", seatName)
										.append("owner", userId)
										.append("status", "pending");//仮押さえ枚数の確認処理の内容
			System.out.println(coll.find(pquery).count());
			//2フェーズ目
			if (coll.find(pquery).count() != count) {//仮押さえ枚数が要求分と同じかチェック
				//通らないのは　１売り切れの時（足りない）　２同時実行の時（多い）
				// 必要枚数確保できていなければ巻き戻し、TicketSoldOutExceptionを投げる
				//コマンド入力
				// db.ticket.update(
				//     {"eventName" : eventName, "seatName" : seatName, "owner" : userId, "status" : "pending"},
				//     {"$set" : {"owner" : "blank", "status" : "blank"}},
				//     false, true
				// )
				DBObject rollback = new BasicDBObject("$set",
						new BasicDBObject("owner", "blank")
								  .append("status", "blank"));//取り消し処理の内容

				coll.updateMulti(pquery, rollback);//元に戻す処理

				String msg = "Sold out.";
				throw new TicketSoldOutException(msg);
			}//失敗時（必要枚数確保できなかったとき、同時にきたとき）の処理


			// 必要枚数確保できていれば"status（状態）"を"reserved（”予約済み”）"に更新する
			//コマンド入力
			// db.ticket.update(
			//     {"eventName" : eventName, "seatName" : seatName, "owner" : userId, "status" : "pending"},
			//     {"$set" : {"status" : "reserved"}},
			//     false, true
			// )
			DBObject reserved = new BasicDBObject("$set",
					new BasicDBObject("status", "reserved"));//予約処理の内容
			coll.updateMulti(pquery, reserved);//”予約済み”に更新
			//GetMessage m = new GetMessage();
			//String n = m.getMessage();
			//System.out.println(n);
		} catch (MongoException e) {
		}//throws宣言よりmainが例外処理を行う
	}

	/*public String getMessage(){
		String m = "Reserved.";
		return m;
	}*/

	public static void main(String[] args) {
		TicketModel ticket = new TicketModel("","","","",1, 1);//インスタンスticketの生成（TicketModelクラス型）
		try {
			ticket.buyTickets("Cloud Spiral 2013", "A", "user3", 2);//ticketを買う手続き、（）内が要求するticketの詳細
			//System.out.println(m.getMessage());
		} catch (TicketSoldOutException e) {
			System.out.println(e.getMessage());//例外時の問題に関する詳細をコンソールに表記（この場合は126行目より"Sold out"）
		}//ticket購入処理およびその例外処理

		List<EventInfo> events = ticket.getVacantSeatNumList();//List生成の命令
		for (EventInfo e: events) {//e内にイベント情報を記入
			System.out.println(e.getEventName() + ": " + e.getVacantSeatNum());
		}//eventsが返される限り、イベント名とその空き情報をコンソールに表記
	}

}
