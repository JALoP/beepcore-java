package org.beepcore.beep.example;

import java.net.InetAddress;

import org.beepcore.beep.core.AbortChannelException;
import org.beepcore.beep.core.BEEPError;
import org.beepcore.beep.core.BEEPException;
import org.beepcore.beep.core.Channel;
import org.beepcore.beep.core.CloseChannelException;
import org.beepcore.beep.core.InputDataStream;
import org.beepcore.beep.core.Message;
import org.beepcore.beep.core.MessageMSG;
import org.beepcore.beep.core.OutputDataStream;
import org.beepcore.beep.core.ProfileRegistry;
import org.beepcore.beep.core.ReplyListener;
import org.beepcore.beep.core.RequestHandler;
import org.beepcore.beep.core.Session;
import org.beepcore.beep.core.StartChannelException;
import org.beepcore.beep.core.StartChannelListener;
import org.beepcore.beep.core.StringOutputDataStream;
import org.beepcore.beep.profile.ProfileConfiguration;
import org.beepcore.beep.profile.echo.EchoProfile;
import org.beepcore.beep.profile.sasl.anonymous.SASLAnonymousProfile;
import org.beepcore.beep.profile.sasl.otp.SASLOTPProfile;
import org.beepcore.beep.profile.sasl.otp.database.UserDatabasePool;
import org.beepcore.beep.transport.tcp.TCPSession;
import org.beepcore.beep.transport.tcp.TCPSessionCreator;
import org.beepcore.beep.util.BufferSegment;

public class SASLExample implements RequestHandler {
	private static final String SAMPLE_OTP_USER = "IW_User";
	private static final String SAMPLE_OTP_PASSPHRASE = "SOME_PASSPHRASE";
	private static final String USAGE = "<hostnam>:<port> [l|i]\n"
			+ "specify the hostname and port, the last argument (the letter 'l' or letter 'i') "
			+ "indicates if it should listen for or initiate the connection.";
	private static final String SAMPLE_ID = "SOME_ID";
	private static UserDatabasePool userDatabasePool;

	/*
	 * Package and import directives omitted for brevity. Local constants
	 * denoted in UPPER_CASE
	 */
	public static void main(String argv[]) {
		try {

			/*
			 * ProfileRegistry is a helpful class. It allows a BEEP Peer to
			 * register various classes (implementations of the class
			 * ChannelControlListener) to process StartChannel requests for a
			 * given profile. In this case, we're creating and initializing
			 * instances of our two SASL profiles, Anonymous and OTP. We then
			 * register these profiles (which are their own Channel Control
			 * Listeners) as the class to be called if a start channel request
			 * is received. We also register the EchoProfile (for reasons we'll
			 * show later). We register CCLs for each profile with the
			 * addChannelControlListener call
			 */
			// Set up our profile registry
			ProfileRegistry reg = new ProfileRegistry();
			SASLAnonymousProfile anon = new SASLAnonymousProfile();
			SASLOTPProfile otp = new SASLOTPProfile();
			// This throws an exception... anon.init(new
			// ProfileConfiguration());
			otp.init(null, new ProfileConfiguration());

			reg.addStartChannelListener(SASLOTPProfile.URI, otp, null);
			reg.addStartChannelListener(SASLAnonymousProfile.uri, anon, null);
			reg.addStartChannelListener(EchoProfile.ECHO_URI,
					new SASLExample().getCCL(), null);

			/*
			 * If the command line arguments specify to make this a 'listener'
			 * then we proceed to set ourselves up appropriately. We have
			 * provided a simple stub routine to generate OTP databases for some
			 * sample users (which are IW_User and IW_User2) to allow the
			 * example to function correctly.
			 */

			// If we're a listener, then create sessions by 'listening' on the
			// static AutomatedTCPSessionCreator methods
			if (argv[2].charAt(0) == 'l') {
				InetAddress addr = null;

				/*
				 * This bit is a little inane, it simple creates a couple of OTP
				 * database files for IW_User and IW_User2. After running this
				 * in listening mode, you can see this files locally. I suggest
				 * you take a look at them. It is our intention to write an
				 * interface through which other storage mechanisms can hook in
				 * to store these things - so that the profile can be used
				 * without being extended. That's rife with security issues
				 * however, so this is what is available for now.
				 */

				// Creates stub accts for the users in this example
				userDatabasePool = new UserDatabasePool();
				try {
					userDatabasePool.populateUserDatabases();
				} catch (BEEPException ex) {
					ex.printStackTrace();
				}

				/*
				 * This is where we bind to an address/port combination and
				 * begin to listen for connections. If another peer connects to
				 * our port and sends us a greeting, a new session is created.
				 */

				try {
					addr = InetAddress.getByName(argv[0]);
				} catch (Exception x) {
					addr = InetAddress.getLocalHost();
				}

				System.out.println("Listening on " + addr.toString() + ":"
						+ argv[1]);

				while (true) {
					Session newSession = TCPSessionCreator.listen(addr,
							Integer.parseInt(argv[1]), reg);

				}
			} else if (argv[2].charAt(0) == 'i') {
				/* This is the initiator path */
				Channel echoChannel = null;

				/*
				 * We create a Session by connecting to the host/port where we
				 * know another BEEP peer is listening. The peers exchange
				 * greetings, and a reference to the Session is returned to us
				 */

				Session session = TCPSessionCreator.initiate(
						InetAddress.getByName(argv[0]),
						Integer.parseInt(argv[1]), reg);

				/*
				 * The routine used below, AuthenticateSASLAnonymous is provided
				 * as a convenience routine. All you have to do is provide it
				 * with your session (the first argument) and some sort of
				 * identifier for yourself (the second argument, in this case,
				 * the 'anonymous' string constant) and go. The return value is
				 * another Session reference. In this case, this will be the
				 * same session - although this isn't always the case.
				 */
				if (argv[3].charAt(0) == 'a') {
					session = SASLAnonymousProfile.AuthenticateSASLAnonymous(
							session, SASLAnonymousProfile.ANONYMOUS);
				}

				/*
				 * This is the OTP convenience routine. The arguments are,
				 * first, the session you wish to Authenticate on, second the
				 * Authorization ID you wish to use (this is who you are
				 * authorized to act as, which is different than who you
				 * authenticate as - see the OTP spec for a more extensive
				 * explanation), third, the identity you're authenticating as,
				 * fourth, the passphrase you use to secure yourself.
				 */
				else if (argv[3].charAt(0) == 'o') {
					session = (TCPSession) SASLOTPProfile.AuthenticateSASLOTP(
							session, "IW_User", // No Authorization ID
							SAMPLE_OTP_USER, SAMPLE_OTP_PASSPHRASE);
				}

				/*
				 * When either SASL authentication routine succeeds without
				 * throwing a SASLException, you're homefree! We can examine the
				 * new credentials we have by calling
				 * session->getMyCredentials() which returns a SessionCredential
				 * object, whose toString() method prettyily prints all its
				 * attributes.
				 */

				/*
				 * What we're going to do now shows us who we are as far as our
				 * peer is concerned. We're going to start up an ECHO profile on
				 * BEEP (a profile that simply echos back what we type) only it
				 * has a twist. The ECHO profile this time (since we're running
				 * our example) echoes back what we sent them AND includes
				 * credential information indicating who we have authenticated
				 * as - from their point of view. This shows how the credential
				 * exists on both sides.
				 */
				echoChannel = session.startChannel(EchoProfile.ECHO_URI);
				String temp = "Hi There!";
				ReplyListener idiot = new ReplyListener() {

					@Override
					public void receiveRPY(Message message)
							throws AbortChannelException {
						dumpMessage("RPY", message);
					}

					@Override
					public void receiveNUL(Message message)
							throws AbortChannelException {
						dumpMessage("NUL", message);

					}

					@Override
					public void receiveERR(Message message)
							throws AbortChannelException {
						dumpMessage("ERR", message);

					}

					@Override
					public void receiveANS(Message message)
							throws AbortChannelException {
						dumpMessage("ANS", message);

					}

					private void dumpMessage(String type, Message message) {
						System.out.print(type + ":");
						InputDataStream ds = message.getDataStream();
						while (true) {
							try {
								BufferSegment b = ds.waitForNextSegment();
								if (b == null) {
									break;
								}
								System.out.print(new String(b.getData()));
							} catch (InterruptedException e) {
								message.getChannel().getSession().terminate(e.getMessage());
								return;
							}
						}
						try {
							message.getChannel().close();
						} catch (BEEPException e) {
							e.printStackTrace();
							System.out.println(e.getMessage());
						}
						try {
							message.getChannel().getSession().close();
						} catch (BEEPException e) {
							e.printStackTrace();
							System.out.println(e.getMessage());
						}

					}
				};
				;
				;
				echoChannel.sendMSG(new StringOutputDataStream(temp), idiot);

				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} else {
				System.out.println(USAGE);
				return;
			}
		} catch (Exception x) {
			x.printStackTrace();
			return;
		}

	}

	private StartChannelListener getCCL() {
		final RequestHandler parent = this;
		return new StartChannelListener() {

			@Override
			public void startChannel(Channel channel, String encoding,
					String data) {
				channel.setRequestHandler(parent);
			}

			@Override
			public void closeChannel(Channel channel)
					throws CloseChannelException {
				channel.setRequestHandler(null);
			}

			@Override
			public boolean advertiseProfile(Session session)
					throws BEEPException {
				return true;
			}
		};
	}

	public void receiveMSG(MessageMSG message) {
		OutputDataStream data = new OutputDataStream();
		InputDataStream ds = message.getDataStream();

		while (true) {
			try {
				BufferSegment b = ds.waitForNextSegment();
				if (b == null) {
					break;
				}
				System.out.print(new String(b.getData()));
				data.add(b);
			} catch (InterruptedException e) {
				message.getChannel().getSession().terminate(e.getMessage());
				return;
			}
		}

		data.setComplete();

		try {
			message.sendRPY(data);
		} catch (BEEPException e) {
			try {
				message.sendERR(BEEPError.CODE_REQUESTED_ACTION_ABORTED,
						"Error sending RPY");
			} catch (BEEPException x) {
				message.getChannel().getSession().terminate(x.getMessage());
			}
			return;
		}
	}
}
