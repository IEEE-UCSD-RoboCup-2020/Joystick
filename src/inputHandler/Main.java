package inputHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import inputHandler.GameController.ControllerInput;

/**
 * Servlet implementation class Main
 */
@WebServlet("/Main")
public class Main extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	static final double HALF_JOY_SIZE = 87.5;
	
	static Gson gson;
	static Nipple n;
	static ControllerInput.Builder cib;
	static InetAddress address;
	static DatagramSocket socket;
	
	@Override
	public void init() throws ServletException {
		gson = new Gson();
		n    = new Nipple();
		cib  = ControllerInput.newBuilder();
		
		try {
			address = InetAddress.getByName("173.230.158.61");
			socket = new DatagramSocket();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}
	
    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
    		throws ServletException, IOException {
    	PrintWriter printWriter = response.getWriter();
    	printWriter.print("Please send data using POST");
    }
    
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {
    	//String x = request.getParameter("x");
    	//String y = request.getParameter("y");
	    StringBuilder sb = new StringBuilder();
	    BufferedReader reader = request.getReader();
	    try {
	        String line;
	        while ((line = reader.readLine()) != null) {
	            sb.append(line).append('\n');
	        }
	    } finally {
	        reader.close();
	    }
	    
	    Nipple temp = gson.fromJson(sb.toString(), Nipple.class);
	    
	    if(temp.left_dist < 0) {
	    	n.righ_dist = temp.righ_dist;
	    	n.righ_angl = temp.righ_angl;
	    } else if(temp.righ_dist < 0) {
	    	n.left_dist = temp.left_dist;
	    	n.left_angl = temp.left_angl;
	    }
	    
	    double left_x = n.left_dist * Math.cos(n.left_angl) / HALF_JOY_SIZE;
	    double left_y = n.left_dist * Math.sin(n.left_angl) / HALF_JOY_SIZE;
	    double righ_x = n.righ_dist * Math.cos(n.righ_angl) / HALF_JOY_SIZE;
	    double righ_y = n.righ_dist * Math.sin(n.righ_angl) / HALF_JOY_SIZE;
	    
	    cib.setXAxisValue((float)righ_x);
		cib.setYAxisValue((float)righ_y);
		cib.setSpinSpeed((float)left_x);
		cib.setShoot(temp.shoot);
		if(temp.shoot) {
			cib.setShootAngle((float)left_y);
		}
		
		ControllerInput ci = cib.build();
		System.out.println(ci);
		
		byte[] buf = ci.toByteArray();
		DatagramPacket packet = new DatagramPacket(buf, buf.length, address, 8889);
		
		try {
			socket.send(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	class Nipple {
		float   left_dist;
		float   left_angl;
		float   righ_dist;
		float   righ_angl;
		boolean shoot;
	}

}
