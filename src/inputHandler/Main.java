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
import inputHandler.VFirmwareAPI.VF_Commands;
import inputHandler.VFirmwareAPI.Vec_2D;

/**
 * Servlet implementation class Main
 */
@WebServlet("/Main")
public class Main extends HttpServlet {

	private static final long serialVersionUID = 1L;
	static final double HALF_JOY_SIZE = 87.5;

	static Gson gson;
	static Nipple n;
	static VF_Commands.Builder vfb;
	static InetAddress address;
	static DatagramSocket socket;

	@Override
	public void init() throws ServletException {
		gson = new Gson();
		n = new Nipple();
		vfb = VF_Commands.newBuilder();

		try {
			address = InetAddress.getByName("45.51.1.182");
			socket = new DatagramSocket();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		PrintWriter printWriter = response.getWriter();
		printWriter.print("Please send data using POST");
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
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

		if (temp.left_dist < 0) {
			n.right_dist = temp.right_dist;
			n.right_angl = temp.right_angl;
		} else if (temp.right_dist < 0) {
			n.left_dist = temp.left_dist;
			n.left_angl = temp.left_angl;
		}

		double left_x = n.left_dist * Math.cos(n.left_angl) / HALF_JOY_SIZE;
		double left_y = n.left_dist * Math.sin(n.left_angl) / HALF_JOY_SIZE;
		double right_x = n.right_dist * Math.cos(n.right_angl) / HALF_JOY_SIZE;
		double right_y = n.right_dist * Math.sin(n.right_angl) / HALF_JOY_SIZE;

		Vec_2D.Builder translationalOutput = Vec_2D.newBuilder();
		translationalOutput.setX((float) right_x);
		translationalOutput.setY((float) right_y);
		vfb.setTranslationalOutput(translationalOutput);

		Vec_2D.Builder kicker = Vec_2D.newBuilder();
		if (temp.shoot_mode) {
			kicker.setX(0.0f);
			vfb.setRotationalOutput(0.0f);
			n.left_angl = 0.0f;
			n.left_dist = 0.0f;
		} else {
			if (temp.shoot) {
				kicker.setX((float) left_x);
				kicker.setY((float) left_y);
				n.left_angl = 0.0f;
				n.left_dist = 0.0f;
			} else {
				vfb.setRotationalOutput((float) left_x);
			}
		}
		kicker.build();
		vfb.setKicker(kicker);
		vfb.setDribbler(true);

		VF_Commands commands = vfb.build();
		System.out.println(commands);

		byte[] buf = commands.toByteArray();
		DatagramPacket packet = new DatagramPacket(buf, buf.length, address, 8889);

		try {
			socket.send(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	class Nipple {
		float left_dist;
		float left_angl;
		float right_dist;
		float right_angl;
		boolean shoot;
		boolean shoot_mode;
	}

}
