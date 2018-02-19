package jp.enpit.cloud.mongo;

import com.mongodb.DB;

public class MyDB {

	private DB db;

	public MyDB(DB db) {
		// TODO 自動生成されたコンストラクター・スタブ
		this.db = db;
	}

	public MyDBCollection getCollection(String collection, String threadName,
			String eventName, String seatName, String userId, int count, int flag) {

		return new MyDBCollection(db.getCollection(collection), threadName,
				eventName, seatName, userId, count, flag);
	}
}
