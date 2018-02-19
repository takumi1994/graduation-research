package jp.enpit.cloud.mongo;

import static org.junit.Assert.*;

public class TicketModelTestThread implements Runnable {

	private String threadName, eventName, seatName, userId;
	private int count;
	private TicketModel ticket ;
	private int flag;

	public TicketModelTestThread (String thread, String ev, String se, String id, int ct, int fl) {
		threadName = thread;
		eventName = ev;
		seatName = se;
		userId = id;
		count = ct;
		flag = fl;
		ticket = new TicketModel(threadName, eventName, seatName, userId, count, flag);
	}

	public void run(){
		try {
			ticket.buyTickets(eventName, seatName, userId, count);
			System.out.println("Reserved. by "+userId);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			String x ="Sold out.";
			assertEquals(x,e.getMessage()+" by "+userId);
		}
	}

}
