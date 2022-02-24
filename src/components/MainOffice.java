package components;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.swing.JPanel;


public class MainOffice implements Runnable{
	static private volatile MainOffice instance = null;
	private static int clock=0;
	private static Hub hub;
	static private ArrayList<Package> packages = new ArrayList<Package>();
//	static private ArrayList<Customer> customers = new ArrayList<Customer>();
	private JPanel panel;
//	private int maxPackages;
	private boolean threadSuspend = false;
	
	private MainOffice(int branches, int trucksForBranch, JPanel panel) {//, int maxPack) {
		this.panel = panel;
//		this.maxPackages = maxPack;
		addHub(trucksForBranch);
		addBranches(branches, trucksForBranch);
		System.out.println("\n\n========================== START ==========================");
	}
	
	public static MainOffice getInstance(int branches, int trucksForBranch, JPanel panel, int maxPack) {
		if(instance == null) {
			synchronized (MainOffice.class) {
				if(instance == null) {
					instance = new MainOffice(branches, trucksForBranch, panel);//, maxPack);
				}
			}
		}
		return instance;
	}
	
	public static Hub getHub() {
		return hub;
	}
	
	public static void addPackage(Package p) {
		packages.add(p);
	}


	public static int getClock() {
		return clock;
	}

	@Override
	public void run() {
		Executor executor = Executors.newFixedThreadPool(4);
		int numberOfCustomers = 10;
		Random r = new Random();
		for(int i = 0;i<numberOfCustomers;i++) {
			executor.execute(new Customer(i + 1, new Address(r.nextInt(MainOffice.getHub().getBranches().size()), r.nextInt(999999)+100000)));
		}
		Thread hubThrad = new Thread(hub);
		hubThrad.start();
		for (Truck t : hub.listTrucks) {
			Thread trackThread = new Thread(t);
			trackThread.start();
		}
		for (Branch b: hub.getBranches()) {
			Thread branch = new Thread(b);
			for (Truck t : b.listTrucks) {
				Thread trackThread = new Thread(t);
				trackThread.start();
			}
			branch.start();
		}
		while(true) {
		    synchronized(this) {
                while (threadSuspend)
					try {
						wait();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		    }
			tick();
		}
		
	}
	
	
	public void printReport() {
		for (Package p: packages) {
			System.out.println("\nTRACKING " +p);
			for (Tracking t: p.getTracking())
				System.out.println(t);
		}
	}
	
	
	public String clockString() {
		String s="";
		int minutes=clock/60;
		int seconds=clock%60;
		s+=(minutes<10) ? "0" + minutes : minutes;
		s+=":";
		s+=(seconds<10) ? "0" + seconds : seconds;
		return s;
	}
	
	
	public void tick() {
		try {
			Thread.sleep(300);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(clockString());
		clock++;
//		if (clock++%5==0 && maxPackages>0) {
////			addPackage(); /// Because of addPackage() in customer
//			maxPackages--;
//		}
//		branchWork(hub);
//		for (Branch b:hub.getBranches()) {
//			branchWork(b);
//		}
		panel.repaint();
	}
	
	
	
	public void branchWork(Branch b) {
		for (Truck t : b.listTrucks) {
			t.work();
		}
		b.work();
	}
	
	
	public void addHub(int trucksForBranch) {
		hub=new Hub();
		for (int i=0; i<trucksForBranch; i++) {
			Truck t = new StandardTruck();
			hub.addTruck(t);
		}
		Truck t=new NonStandardTruck();
		hub.addTruck(t);
	}
	
	
	public void addBranches(int branches, int trucks) {
		for (int i=0; i<branches; i++) {
			Branch branch=new Branch();
			for (int j=0; j<trucks; j++) {
				branch.addTruck(new Van());
			}
			hub.add_branch(branch);		
		}
	}
	
	
	public static ArrayList<Package> getPackages(){
		return packages;
	}
	
//	public void addPackage() {
//		Random r = new Random();
//		Package p;
//		Branch br;
//		Priority priority=Priority.values()[r.nextInt(3)];
//		Address sender = new Address(r.nextInt(hub.getBranches().size()), r.nextInt(999999)+100000);
//		Address dest = new Address(r.nextInt(hub.getBranches().size()), r.nextInt(999999)+100000);
//
//		switch (r.nextInt(3)){
//		case 0:
//			p = new SmallPackage(priority,  sender, dest, r.nextBoolean() );
//			br = hub.getBranches().get(sender.zip);
//			br.addPackage(p);
//			p.setBranch(br); 
//			break;
//		case 1:
//			p = new StandardPackage(priority,  sender, dest, r.nextFloat()+(r.nextInt(9)+1));
//			br = hub.getBranches().get(sender.zip); 
//			br.addPackage(p);
//			p.setBranch(br); 
//			break;
//		case 2:
//			p=new NonStandardPackage(priority,  sender, dest,  r.nextInt(1000), r.nextInt(500), r.nextInt(400));
//			hub.addPackage(p);
//			break;
//		default:
//			p=null;
//			return;
//		}
//		
//		this.packages.add(p);
//		
//	}
	
	
	public synchronized void setSuspend() {
	   	threadSuspend = true;
		for (Truck t : hub.listTrucks) {
			t.setSuspend();
		}
		for (Branch b: hub.getBranches()) {
			for (Truck t : b.listTrucks) {
				t.setSuspend();
			}
			b.setSuspend();
		}
		hub.setSuspend();
	}

	
	
	public synchronized void setResume() {
	   	threadSuspend = false;
	   	notify();
	   	hub.setResume();
		for (Truck t : hub.listTrucks) {
			t.setResume();
		}
		for (Branch b: hub.getBranches()) {
			b.setResume();
			for (Truck t : b.listTrucks) {
				t.setResume();;
			}
		}
	}



}
