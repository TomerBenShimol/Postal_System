package components;

import java.util.Random;

public class Customer implements Runnable{
	private int serialNumber;
	private Address address;
	private int packageNumber;
	
	public Customer (int sn, Address ad) {
		serialNumber = sn;
		address = ad;
		packageNumber = 0;
	}
	
	public void addPackage() {
		Random r = new Random();
		Package p;
		Branch br;
		Priority priority=Priority.values()[r.nextInt(3)];
		Address sender = address;
		Address dest = new Address(r.nextInt(MainOffice.getHub().getBranches().size()), r.nextInt(999999)+100000);
		switch (r.nextInt(3)){
		case 0:
			p = new SmallPackage(priority,  sender, dest, r.nextBoolean(),serialNumber);
			br = MainOffice.getHub().getBranches().get(sender.zip);
			br.addPackage(p);
			p.setBranch(br); 
			break;
		case 1:
			p = new StandardPackage(priority,  sender, dest, r.nextFloat()+(r.nextInt(9)+1),serialNumber);
			br = MainOffice.getHub().getBranches().get(sender.zip); 
			br.addPackage(p);
			p.setBranch(br); 
			break;
		case 2:
			p=new NonStandardPackage(priority,  sender, dest,  r.nextInt(1000), r.nextInt(500), r.nextInt(400),serialNumber);
			MainOffice.getHub().addPackage(p);
			break;
		default:
			p=null;
			return;
		}
		MainOffice.addPackage(p);
	}
	
	public boolean deliverd() {
		for(Package p : MainOffice.getPackages()) {
			if(p.getCustomerNumber() == this.serialNumber) {
				if(!(p.getStatus().equals(Status.DELIVERED))) {
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public void run() {
		synchronized (this) {
			Random r1 = new Random();
			while(packageNumber < 5) {
				try {
					Thread.sleep((2 + r1.nextInt(4)) * 1000);
				} catch (Exception e) {}
				addPackage();
				packageNumber++;
			}
			packageNumber = 0;
			while(!deliverd()) {
				try {
					wait();
				} catch (Exception e) {}
			}
		}
	}
}
