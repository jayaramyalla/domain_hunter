package burp;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.border.EmptyBorder;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JTextArea;
import java.awt.Font;
import java.awt.FlowLayout;
import javax.swing.JSplitPane;
import java.awt.Component;
import java.awt.Desktop;
import javax.swing.Box;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.PrintWriter;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import javax.swing.JScrollPane;


import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import burp.BurpExtender;
import burp.Commons.*;

public class GUI extends JFrame {
	
    public String ExtenderName = "Domain Hunter v1.1 by bit4";
    public String github = "https://github.com/bit4woo/domain_hunter";
    private String summary = "      Sub-domain:%s  Similar-domain:%s  Related-domain:%s  ^_^";
    public Set<String> subdomainofset = new HashSet<String>();
    public Set<String> domainlikeset = new HashSet<String>();
    public Set<String> relatedDomainSet = new HashSet<String>();
    public String resultJson;
    
    private PrintWriter stdout;
    private PrintWriter stderr;
    
	private JPanel contentPane;
	private JTextField textFieldSubdomains;
	private JTextField textFieldDomainsLike;
	private JTextField textFieldUploadURL;
	private JLabel lblSubDomainsOf;
	private JButton btnSearch;
	private JButton btnUpload;
	private JButton btnSpiderAll;
	private JLabel lblSummary;
	private JPanel panel_2;
	private JLabel lblNewLabel_2;
	private JSplitPane splitPane;
	private Component verticalStrut;
	private JTextArea textArea;
	private JTextArea textArea_1;
	private JTextArea textArea_2;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					GUI frame = new GUI();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public GUI() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 930, 497);
		contentPane =  new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		
		JPanel panel = new JPanel();
		FlowLayout flowLayout_1 = (FlowLayout) panel.getLayout();
		flowLayout_1.setAlignment(FlowLayout.LEFT);
		contentPane.add(panel, BorderLayout.NORTH);
		
		lblSubDomainsOf = new JLabel("SubDomains of  ");
		panel.add(lblSubDomainsOf);
		
		textFieldSubdomains = new JTextField();
		textFieldSubdomains.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent arg0) {
				String domain = textFieldSubdomains.getText();
				textFieldDomainsLike.setText(domain.substring(0,domain.lastIndexOf(".")));
			}
		});
		panel.add(textFieldSubdomains);
		textFieldSubdomains.setColumns(20);
		
		verticalStrut = Box.createVerticalStrut(20);
		panel.add(verticalStrut);
		
		JLabel lblDomainsLike = new JLabel("Domains like ");
		panel.add(lblDomainsLike);
		
		textFieldDomainsLike = new JTextField();
		panel.add(textFieldDomainsLike);
		textFieldDomainsLike.setColumns(20);
		
		JLabel lblUploadURL = new JLabel("Upload URL ");
		panel.add(lblUploadURL);
		
		textFieldUploadURL = new JTextField("http://");
		panel.add(textFieldUploadURL);
		textFieldUploadURL.setColumns(20);
		
		btnSearch = new JButton("Search");
		btnSearch.setToolTipText("Do a single search from site map");
		btnSearch.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				SwingWorker<Map, Map> worker = new SwingWorker<Map, Map>() {
			    	//using SwingWorker to prevent blocking burp main UI.

			        @Override
			        protected Map doInBackground() throws Exception {                
						String subdomain = textFieldSubdomains.getText();
						String domainlike = textFieldDomainsLike.getText();
						btnSearch.setEnabled(false);
						return search(subdomain,domainlike);
			        }
			        @Override
			        protected void done() {
			            try {
				        	Map result = get();
				        	subdomainofset = (Set) result.get("subdomainofset"); //之前的set变成了object
				        	domainlikeset = (Set) result.get("domainlikeset");
				        	relatedDomainSet = (Set) result.get("relatedDomainSet");
							textArea.setText(Commons.set2string(subdomainofset));
							textArea_1.setText(Commons.set2string(domainlikeset));
							textArea_2.setText(Commons.set2string(relatedDomainSet));
							btnSearch.setEnabled(true);
							lblSummary.setText(String.format(summary, subdomainofset.size(),domainlikeset.size(),relatedDomainSet.size()));
			            } catch (Exception e) {
			            	btnSearch.setEnabled(true);
			                e.printStackTrace(stderr);
			            }
			        }
			    };      
			    worker.execute();
				
			}
		});
		panel.add(btnSearch);
		
		btnSpiderAll = new JButton("Spider All");
		btnSpiderAll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				
			    SwingWorker<Map, Map> worker = new SwingWorker<Map, Map>() {
			    	//可以在一个类中实现另一个类，直接实现原始类，没有变量处理的困扰；
			    	//之前的想法是先单独实现一个worker类，在它里面处理各种，就多了一层实现，然后在这里调用，变量调用会是一个大问题。
			    	//https://stackoverflow.com/questions/19708646/how-to-update-swing-ui-while-actionlistener-is-in-progress
			        @Override
			        protected Map doInBackground() throws Exception {                
						String subdomain = textFieldSubdomains.getText();
						String domainlike = textFieldDomainsLike.getText();
						//stdout.println(subdomain);
						//stdout.println(domainlike);
						btnSpiderAll.setEnabled(false);
						return spiderall(subdomain,domainlike);
					
			        }
			        @Override
			        protected void done() {
			            try {
				        	Map result = get();
				        	subdomainofset = (Set<String>) result.get("subdomainofset"); //之前的set变成了object
				        	domainlikeset = (Set<String>) result.get("domainlikeset");
							textArea.setText(Commons.set2string(subdomainofset));
							textArea_1.setText(Commons.set2string(domainlikeset));
							btnSpiderAll.setEnabled(true);
			            } catch (Exception e) {
			                e.printStackTrace(stderr);
			            }
			        }
			    };
			    worker.execute();
			}
		});
		btnSpiderAll.setToolTipText("Spider all subdomains recursively,This may take a long time!!!");
		panel.add(btnSpiderAll);
		
		
		btnUpload = new JButton("Upload");
		btnUpload.setToolTipText("Do a single search from site map");
		btnUpload.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				SwingWorker<Boolean, Boolean> worker = new SwingWorker<Boolean, Boolean>() {
			        @Override
			        protected Boolean doInBackground() throws Exception {                
			        	return upload("","");
			        }
			        @Override
			        protected void done() {
			        }
			    };  
			    worker.execute();
				
			}
		});
		panel.add(btnUpload);
		
		lblSummary = new JLabel("      ^_^");
		panel.add(lblSummary);
		
		splitPane = new JSplitPane();
		splitPane.setDividerLocation(0.5);
		contentPane.add(splitPane, BorderLayout.WEST);
		
		textArea = new JTextArea();
		textArea.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				final JPopupMenu jp = new JPopupMenu();
		        jp.add("^_^");
		        textArea.addMouseListener(new MouseAdapter() {
		            @Override
		            public void mouseClicked(MouseEvent e) {
		                if (e.getButton() == MouseEvent.BUTTON3) {
		                    // 弹出菜单
		                    jp.show(textArea, e.getX(), e.getY());
		                }
		            }
		        });
			}
		});
		textArea.setColumns(30);
		splitPane.setLeftComponent(textArea);
		
		textArea_1 = new JTextArea();
		textArea_1.setColumns(30);
		splitPane.setRightComponent(textArea_1);
		
		JSplitPane splitPane_1 = new JSplitPane();
		splitPane.setDividerLocation(0.5);
		contentPane.add(splitPane_1, BorderLayout.EAST);
		
		textArea_2 = new JTextArea();
		textArea_2.setColumns(30);
		splitPane_1.setLeftComponent(textArea_2);
		
		panel_2 = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel_2.getLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);
		contentPane.add(panel_2, BorderLayout.SOUTH);
		
		lblNewLabel_2 = new JLabel(ExtenderName+"    "+github);
		lblNewLabel_2.setFont(new Font("宋体", Font.BOLD, 12));
		lblNewLabel_2.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				try {
					URI uri = new URI(github);
					Desktop desktop = Desktop.getDesktop();
					if(Desktop.isDesktopSupported()&&desktop.isSupported(Desktop.Action.BROWSE)){
						desktop.browse(uri);
					}
				} catch (Exception e2) {
					e2.printStackTrace(stderr);
				}
				
			}
			@Override
			public void mouseEntered(MouseEvent e) {
				lblNewLabel_2.setForeground(Color.BLUE);
			}
			@Override
			public void mouseExited(MouseEvent e) {
				lblNewLabel_2.setForeground(Color.BLACK);
			}
		});
		panel_2.add(lblNewLabel_2);
	}
	
	public Map<String, Set<String>> spiderall (String subdomainof, String domainlike) {
	    System.out.println("spiderall testing... you need to over write this function!");
	    return null;
	}
	
	
	public Map<String, Set<String>> search(String subdomainof, String domainlike){
		System.out.println("search testing... you need to over write this function!");
		return null;
	}
	public Boolean upload(String url,String resultJson) {
		System.out.println("upload testing... you need to over write this function!");
		return null;
	}
}
