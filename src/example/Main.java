package example;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.cometd.bayeux.server.BayeuxServer;
import org.cometd.bayeux.server.ConfigurableServerChannel;
import org.cometd.bayeux.server.ServerChannel;
import org.cometd.server.CometdServlet;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import sun.misc.BASE64Encoder;

public class Main {

	private static final ScheduledExecutorService exec = Executors.newScheduledThreadPool(1);
	public static void main(String[] args) throws Exception{
		Server server = new Server(8080);
		ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
		context.setContextPath("/");
		server.setHandler(context);
		
		CometdServlet servlet = new CometdServlet();
		context.addServlet(new ServletHolder(servlet), "/bayeux");
		server.start();
		
		BayeuxServer bayeux = servlet.getBayeux();
		String channelName = "/data";
		bayeux.createIfAbsent(channelName, new ServerChannel.Initializer() {
			public void configureChannel(ConfigurableServerChannel channel) {
				channel.setPersistent(true);
				
			}
		});
		final ServerChannel channel = bayeux.getChannel(channelName);
		
		byte[] bytes = new byte[40];
		for(int i=0; i<bytes.length; ++i) {
			bytes[i] = (byte)(Math.random() * 255);
		}
		
		BASE64Encoder base64Encoder = new sun.misc.BASE64Encoder();
		final String data = base64Encoder.encode(bytes);
		
		final Map<String, String> firstData = new HashMap<>();
		firstData.put("a", "b");
		
		/*
		 * { a: "b" }
		 */
		
		final Map<String, String> secondData = new HashMap<>();
		secondData.put("base64data", data);
		
		exec.schedule(new Runnable() {
			
			@Override
			public void run() {

				if(Math.random() < 0.5) {
					channel.publish(null, firstData, "1");
					
				} else {
					channel.publish(null, secondData, "2"); 
				}
			}
		}, 1, TimeUnit.SECONDS);
		
		server.join();
		
	}

}
