package jp.enpit.cloud.mongo;

import java.net.UnknownHostException;

import com.mongodb.MongoClient;

public class MyMongoClient {

	private MongoClient m;

	public MyMongoClient(String string, int i) throws UnknownHostException {
		m = new MongoClient(string, i);
	}

	public MyDB getDB(String string) {
		return new MyDB(m.getDB(string));
	}
}
