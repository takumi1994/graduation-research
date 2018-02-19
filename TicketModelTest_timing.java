package jp.enpit.cloud.mongo;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TicketModelTest_timing {

	@Test
	public void test() throws Exception{
		String name = "main";
		String event = "Cloud Spiral 2014";
		String seat = "B";
		String userId = "user4";
		int count = 10;
		int flag = 0;

		TicketModel ticket = new TicketModel(name, event, seat, userId, count, flag);
		TicketModelTestThread task1 = new TicketModelTestThread
										("threadA", "Cloud Spiral 2014", "B", "user3", 11, flag);
		Thread athread = new Thread(task1);//予約するスレッド
		athread.start();//予約開始
		try{
			Thread.sleep(500);
			ticket.buyTickets(event, seat, userId, count);//main
			System.out.println("Reserved. by "+userId);
		}catch (Exception e) {
			System.out.println(e.getMessage()+" by "+userId);
			String x ="Sold out.";
			assertEquals(x,e.getMessage());
		}
	}

}
