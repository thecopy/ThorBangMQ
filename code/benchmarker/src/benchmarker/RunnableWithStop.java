package benchmarker;

public abstract class RunnableWithStop implements Runnable {

	public boolean stop = false;
	@Override
	public abstract void run();

}
