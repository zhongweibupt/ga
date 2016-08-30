/**
 * 
 */
package tianchi.ga.controller.node;

/**
* <p>Title: Action.java</p>
* <p>Description: </p>
* <p>Copyright: Copyright (c) 2007</p>
* <p>Company: Zhongwei</p>
* @author Zhongwei
* @date 2016年8月15日
* @version 1.0
*/
public class Action {
	public String Courier_id;
	public String Addr;
	public int Arrival_time;
	public int Departure;
	public String Amount;
	public String Order_id;
	
	public String action;

	public Action(
		String Courier_id,
		String Addr,
		int Arrival_time,
		int Departure,
		int Amount,
		String Order_id) {
		
		this.Courier_id =Courier_id;
		this.Addr =Addr;
		this.Arrival_time = Arrival_time;
		this.Departure = Departure;
		this.Amount = Amount + "";
		this.Order_id = Order_id; 
		this.action = new String();
	}
	
	public void updateCourierId(String id) {
		this.Courier_id = id;
	}
	
	public void updateArrival_time(int time) {
		this.Arrival_time = time;
	}
	
	public String getAddr() {
		return this.Addr;
	}
	
	public String getAction() {
		this.action = this.Courier_id + "," + 
				this.Addr + "," + 
				this.Arrival_time + "," + 
				this.Departure + "," + 
				this.Amount + "," + 
				this.Order_id;
		return this.action;
	}
}
