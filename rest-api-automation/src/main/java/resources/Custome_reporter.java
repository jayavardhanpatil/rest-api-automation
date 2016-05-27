package com.Company.common;

import org.testng.IInvokedMethod;
import org.testng.IReporter;
import org.testng.IResultMap;
import org.testng.ISuite;
import org.testng.ISuiteResult;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.collections.Lists;
import org.testng.internal.Utils;
import org.testng.log4testng.Logger;
import org.testng.reporters.util.StackTraceTools;
import org.testng.xml.XmlSuite;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class Custome_Reporter implements IReporter {
	private static final Logger L = Logger.getLogger(Custome_Reporter.class);

	// ~ Instance fields ------------------------------------------------------
	private String startTime;
	private String endTime;
	private PrintWriter m_out;
	private int m_methodIndex;
	private int stepsIndex = 0;
	StringBuffer json = new StringBuffer();

	// ~ Methods --------------------------------------------------------------

	/** Creates summary of the run */
	public void generateReport(List<XmlSuite> xml, List<ISuite> suites, String outdir) {
		try {
			m_out = createWriter(outdir);
		} catch (IOException e) {
			L.error("output file", e);
			return;
		}
		startHtml(m_out);
		generateSuiteSummaryReport(suites);
		generateMethodSummaryReport(suites);
		endHtml(m_out);
		m_out.flush();
		m_out.close();
	}

	protected PrintWriter createWriter(String outdir) throws IOException {
		new File(outdir).mkdirs();

		return new PrintWriter(new BufferedWriter(new FileWriter(new File(outdir, "test-report.html"))));
	}

	/**
	 * Creates a table showing the highlights of each test method with links to
	 * the method details
	 */
	protected void generateMethodSummaryReport(List<ISuite> suites) {
		StringBuffer suit = new StringBuffer();
		StringBuffer tests = new StringBuffer();
		StringBuffer method = new StringBuffer();
		StringBuffer steps = new StringBuffer();
		suit.append("<div id=\"detail\"><div class=\"suitenametag\"><span id=\"suitTitle\">Suit Name :</span>");
		int suitIndex = 0;
		m_methodIndex = 0;
		// startResultSummaryTable("passed");

		// suit.append("</div>"); add at the end of loop
		tests.append("<div id=\"tests\">");
		method.append(
				"<div id=\"testMethods\"><table><thead><tr><th>Test Methods</th></tr></thead><tbody class=\"methodpanel active\"></tbody>");
		steps.append(
				"<div id=\"testSteps\"><table><thead><tr><th>Steps detail</th></tr></thead><tbody class=\"steppanel active\"></tbody>");
		for (ISuite suite : suites) {
			suitIndex++;
			if (suitIndex == 1) {
				suit.append("<li rel=\"test" + suitIndex + "\" class=\"active\">" + suite.getName() + "</li>");
			} else {
				suit.append("<li rel=\"test" + suitIndex + "\"" + suite.getName() + "</li>");
			}

			Map<String, ISuiteResult> r = suite.getResults();
			boolean flag = true;
			for (ISuiteResult r2 : r.values()) {
				ITestContext testContext = r2.getTestContext();
				String testName = testContext.getName();
				m_methodIndex++;
				if (suitIndex == 1 && flag) {
					tests.append("<div id=\"test" + suitIndex
							+ "\"class=\"panel active\"><span id=\"TestTitle\">Test Name :</span>");
					flag = false;
				} else if (suitIndex > 1 && flag) {
					tests.append("<div id=\"test" + suitIndex
							+ "\"class=\"panel\"><span id=\"TestTitle\">Test Name :</span>");
					flag = false;
				}
				if (testContext.getFailedTests().size() > 0) {
					tests.append("<li rel=\"method" + m_methodIndex + "\">" + testName
							+ "<img src=\"failed.png\" align=\"right\"></li>");
				} else if (testContext.getSkippedTests().size() > 0) {
					tests.append("<li rel=\"method" + m_methodIndex + "\">" + testName
							+ "<img src=\"skipped.png\" align=\"left\"></li>");
				} else {
					tests.append("<li rel=\"method" + m_methodIndex + "\">" + testName
							+ "<img src=\"passed.png\" align=\"left\"></li>");
				}
				method.append("<tbody id=\"method" + m_methodIndex + "\" class=\"methodpanel\">");

				detailedReport(testContext.getFailedTests(), suite, method, steps, "Failed");
				detailedReport(testContext.getPassedTests(), suite, method, steps, "Passed");
				detailedReport(testContext.getSkippedTests(), suite, method, steps, "Skipped");

			}
			tests.append("</div>");
		}
		steps.append("</table></div>");
		suit.append("</div>");
		tests.append("</div>");
		method.append("</table></div>");
		suit.append(tests);
		suit.append(method);
		suit.append(steps);
		// remove and keep correct position
		m_out.print(suit.toString());

	}

	private void detailedReport(IResultMap resultmap, ISuite suite, StringBuffer method, StringBuffer steps,
			String status) {
		// TODO Auto-generated method stub
		if (resultmap.size() >= 0) {
			int mq = 0;
			String style = "";
			if (status.equals("Passed")) {
				style = "<span style=\"background-color:green;color:white;display:block;margin-left:-10px;padding:5px;margin-top:-5px;float:left;\">Passed</span>";
			} else if (status.equals("Failed")) {
				style = "<span style=\"background-color:red;color:white;display:block;margin-left:-10px;padding:5px;margin-top:-5px;float:left;\">Failed</span>";
			} else if (status.equals("Skipped")) {
				style = "<span style=\"background-color:#d3d3d3;display:block;margin-left:-10px;padding:5px;margin-top:-5px;float:left;\">Skipped</span>";
			}
			List<ITestNGMethod> methodList = new ArrayList<ITestNGMethod>();
			for (ITestNGMethod method2 : getMethodSet(resultmap, suite)) {
				if (!methodList.contains(method2)) {
					for (ITestResult result : resultmap.getResults(method2)) {
						long end = Long.MIN_VALUE;
						long start = Long.MAX_VALUE;
						if (result.getEndMillis() > end) {
							end = result.getEndMillis();
						}
						if (result.getStartMillis() < start) {
							start = result.getStartMillis();
						}
						stepsIndex++;
						methodList.add(method2);
						Object[] parameters = result.getParameters();
						boolean hasParameters = parameters != null && parameters.length > 0;
						if (hasParameters) {
							if (mq == 0) {
								method.append("<tr><td>" + style + "</td></tr>");
								method.append("<tr><td><a rel=\"step" + stepsIndex + "\"class=\"active\">"
										+ method2.getMethodName());
							}
							mq += 1;
							if (mq > 1) {
								method.append("<tr><td><a rel=\"step" + stepsIndex + "\">" + method2.getMethodName());
							}
							method.append(" (");
							for (Object p : parameters) {
								method.append(toString(p) + ",");
							}
							method.replace(method.length() - 1, method.length(), "");
							method.append(")<div style=\"float:right\">" + (end - start) + "ms</div></a></td></tr>");

						} else {
							methodList.clear();
							if (mq == 0) {
								method.append("<tr><td>" + style + "</td></tr>");
								method.append("<tr><td><a rel=\"step" + stepsIndex + "\">" + method2.getMethodName());
							}
							mq += 1;
							if (mq > 1) {
								method.append("<tr><td><a rel=\"step" + stepsIndex + "\">" + method2.getMethodName());
							}
							method.append("<div style=\"float:right\">" + (end - start) + "ms</div></a></td></tr>");
						}
						
						List<String> msgs = Reporter.getOutput(result);
						boolean hasReporterOutput = msgs.size() > 0;
						Throwable exception = result.getThrowable();
						boolean hasThrowable = exception != null;
						if (hasReporterOutput || hasThrowable) {
							steps.append("<tbody id=\"step" + stepsIndex + "\" class=\"steppanel\">");
							if (hasReporterOutput) {
								for (String line : msgs) {
									steps.append(line);
								}
								/*if (hasThrowable) {
									
								}*/
							}
							if (hasThrowable) {
								boolean wantsMinimalOutput = result.getStatus() == ITestResult.SUCCESS;
								if (hasReporterOutput) {
									steps.append(wantsMinimalOutput ? "Expected Exception " : "<br />Failure<br />");
								}

								if (status.equals("Skipped")) {
									steps.append(
											"<tr><td><B>Skipped</B> :<br /><br /> as dependent test is not finished successfully...<br /></td></tr>");
									break;
								}
								generateExceptionReport(exception, method2, steps);
							}
						}
						if (status.equals("Skipped")) {
							steps.append("<tbody id=\"step" + stepsIndex + "\" class=\"steppanel\">");
							steps.append(
									"<tr><td><B>Skipped</B> :<br /> as dependent test is not finished successfully...<br /></td></tr>");
							break;
						}
						
						steps.append("</tbody>");
					}
				}
			}
		}
	}

	

	protected void generateExceptionReport(Throwable exception, ITestNGMethod method, StringBuffer steps) {
		generateExceptionReport(exception, method, exception.getLocalizedMessage(), steps);
	}

	private void generateExceptionReport(Throwable exception, ITestNGMethod method, String title, StringBuffer steps) {
		steps.append("" + Utils.escapeHtml(title) + "<br />");
		StackTraceElement[] s1 = exception.getStackTrace();
		Throwable t2 = exception.getCause();
		if (t2 == exception) {
			t2 = null;
		}
		int maxlines = Math.min(100, StackTraceTools.getTestRoot(s1, method));
		for (int x = 0; x <= maxlines; x++) {
			steps.append((x > 0 ? "<br/>at " : "") + Utils.escapeHtml(s1[x].toString()));
		}
		if (maxlines < s1.length) {
			steps.append("<br/>" + (s1.length - maxlines) + " lines not shown</td></tr>");
		}
		if (t2 != null) {
			generateExceptionReport(t2, method, "Caused by " + t2.getLocalizedMessage(), steps);
		}
	}

	/**
	 * Since the methods will be sorted chronologically, we want to return the
	 * ITestNGMethod from the invoked methods.
	 */
	private Collection<ITestNGMethod> getMethodSet(IResultMap tests, ISuite suite) {
		List<IInvokedMethod> r = Lists.newArrayList();
		List<IInvokedMethod> invokedMethods = suite.getAllInvokedMethods();
		for (IInvokedMethod im : invokedMethods) {
			if (tests.getAllMethods().contains(im.getTestMethod())) {
				r.add(im);
			}
		}
		Arrays.sort(r.toArray(new IInvokedMethod[r.size()]), new TestSorter());
		List<ITestNGMethod> result = Lists.newArrayList();

		// Add all the invoked methods
		for (IInvokedMethod m : r) {
			result.add(m.getTestMethod());
		}

		// Add all the methods that weren't invoked (e.g. skipped) that we
		// haven't added yet
		for (ITestNGMethod m : tests.getAllMethods()) {
			if (!result.contains(m)) {
				result.add(m);
			}
		}
		return result;
	}

	public void generateSuiteSummaryReport(List<ISuite> suites) {
		StringBuffer summerytable = new StringBuffer();
		StringBuffer totalTC = new StringBuffer();
		StringBuffer environment = new StringBuffer();

		NumberFormat formatter = new DecimalFormat("#,##0.0");
		int qty_pass_m = 0;
		int qty_skip = 0;
		int qty_fail = 0;
		int suitcount = 0;
		long time_start = Long.MAX_VALUE;
		long time_end = Long.MIN_VALUE;
		for (ISuite suite : suites) {
			// if (suites.size() > 1) {
			// titleRow(suite.getName(), 7);
			summerytable
					.append("<div style=\"background-color:#d84e55; padding:10px; color:white; width:40%; margin-left:0.73%; text-align:center\">Suit Name : "
							+ suite.getName() + "</div>");
			// }
			summerytable.append("<div id = \"testsummery\">" + "<table = \"summerytable\"><thead>" + "<tr>"
					+ "<th>Test</th>" + "<th>#Passed</th>" + "<th>#skipped</th>" + "<th>#failed</th>"
					+ "<th>Total Time</th>" + "</tr>" + "</thead><tbody>");
			Map<String, ISuiteResult> tests = suite.getResults();
			for (ISuiteResult r : tests.values()) {
				summerytable.append("<tr>");
				ITestContext overview = r.getTestContext();
				if (overview.getFailedTests().size() > 0) {
					summerytable.append(
							"<td>" + overview.getName() + "<img src=\"failed.png\" alt='failed' align=\"left\"></td>");
				} else if (overview.getSkippedTests().size() > 0) {
					summerytable.append("<td>" + overview.getName()
							+ "<img src=\"skipped.png\" alt='skipped' align=\"left\"></td>");
				} else {
					summerytable.append(
							"<td>" + overview.getName() + "<img src=\"passed.png\" alt='passed' align=\"left\"></td>");
				}
				int q = overview.getPassedTests().size();
				qty_pass_m += q;
				summerytable.append("<td>" + q + "</td>");

				/*
				 * q = overview.getPassedTests().size(); qty_pass_s += q;
				 * summaryCell(q, Integer.MAX_VALUE);
				 */

				q = overview.getSkippedTests().size();
				qty_skip += q;
				summerytable.append("<td>" + q + "</td>");
				q = overview.getFailedTests().size();
				qty_fail += q;
				summerytable.append("<td>" + q + "</td>");
				if (suitcount == 0) {
					startTime = overview.getStartDate().toString();
					System.out.println("Start Time : " + startTime);
					String time[] = startTime.split(" ");
					startTime = time[3];
				}
				suitcount++;
				endTime = overview.getEndDate().toString();
				String endTimes[] = endTime.split(" ");
				endTime = endTimes[3];
				time_start = Math.min(overview.getStartDate().getTime(), time_start);
				time_end = Math.max(overview.getEndDate().getTime(), time_end);
				String totalTime = formatter
						.format((overview.getEndDate().getTime() - overview.getStartDate().getTime()) / 1000.);
				/*
				 * Float time = Float.valueOf(totalTime); if(time>60){
				 * summerytable.append("<td>"+(int)(time/60)+"minutes :"
				 * +(int)(time%60)+" seconds </td>"); }else{
				 * summerytable.append("<td>"+ totalTime + "seconds</td>"); }
				 */
				summerytable.append("<td>" + totalTime + "seconds</td>");
				summerytable.append("</tr>");
			}
			summerytable.append("</tbody></table></div></br>");
		}
		totalTC.append("<div id=\"summary\">");
		totalTC.append("<div id=\"summeryBox\">");
		totalTC.append("<div id=\"box\"><span id=\"boxname\">Total Tests</span>");
		int totalTc = qty_pass_m + qty_skip + qty_fail;
		totalTC.append("<span id=\"value\">" + totalTc + "</span></div>");
		totalTC.append("<div id=\"box\"><span id=\"boxname\">Tests Passed</span>");
		totalTC.append("<span id=\"value\">" + qty_pass_m + "</span></div>");
		totalTC.append("<div id=\"box\"><span id=\"boxname\">Tests Skipped</span>");
		totalTC.append("<span id=\"value\">" + qty_skip + "</span></div>");
		totalTC.append("<div id=\"box\"><span id=\"boxname\">Tests Failed</span>");
		totalTC.append("<span id=\"value\">" + qty_fail + "</span></div>");

		// to do need to code to start time and end time.
		totalTC.append("<div id=\"box\"><span id=\"boxname\">Start Time</span>");
		totalTC.append("<span id=\"value\">" + startTime + "</span></div>");
		totalTC.append("<div id=\"box\"><span id=\"boxname\">End Time</span>");
		totalTC.append("<span id=\"value\">" + endTime + "</span></div>");

		totalTC.append("</div>");

		//
		/*
		 * summaryCell(formatter.format((time_end - time_start) / 1000.) +
		 * " seconds", true); m_out.println("<td colspan=\"2\">&nbsp;</td></tr>"
		 * );
		 * 
		 * m_out.println("</div>>");
		 */
		totalTC.append(summerytable);
		try {
			environment.append("<div class=\"environment\"><table><thead><tr><th>Param</th><th>Value</th></tr></thead>"
					+ "<tbody><tr><td>OS</td><td>" + System.getProperty("os.name")
					+ "</td></tr><tr><td>User Name</td><td>" + System.getProperty("user.name") + "</td></tr>"
					+ "<tr><td>Java Version</td><td>" + System.getProperty("java.version")
					+ "</td></tr><tr><td>Host Name</td><td>" + InetAddress.getLocalHost().getHostName() + "</td>"
					+ "</tr></tbody></table></div></div>");

		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		totalTC.append(environment);
		m_out.print(totalTC.toString());
	}

	
	String toString(Object obj) {
		String result;
		if (obj != null) {
			if (obj instanceof boolean[]) {
				result = Arrays.toString((boolean[]) obj);
			} else if (obj instanceof byte[]) {
				result = Arrays.toString((byte[]) obj);
			} else if (obj instanceof char[]) {
				result = Arrays.toString((char[]) obj);
			} else if (obj instanceof double[]) {
				result = Arrays.toString((double[]) obj);
			} else if (obj instanceof float[]) {
				result = Arrays.toString((float[]) obj);
			} else if (obj instanceof int[]) {
				result = Arrays.toString((int[]) obj);
			} else if (obj instanceof long[]) {
				result = Arrays.toString((long[]) obj);
			} else if (obj instanceof Object[]) {
				result = Arrays.deepToString((Object[]) obj);
			} else if (obj instanceof short[]) {
				result = Arrays.toString((short[]) obj);
			} else {
				result = obj.toString();
			}
		} else {
			result = "null";
		}
		return Utils.escapeHtml(result);
	}

	protected void writeStyle(String[] formats, String[] targets) {

	}

	/** Starts HTML stream */
	protected void startHtml(PrintWriter out) {
		out.println("<!DOCTYPE html>\n" + "<html lang=\"en\">\n" + "<head>\n" + "<meta charset=\"utf-8\">\n"
				+ "<title>Test Automation</title>\n"
				+ "<script type=\"text/javascript\" src=\"https://ajax.googleapis.com/ajax/libs/jquery/1.8.3/jquery.min.js\"></script>\n"
				+ "<script type=\"text/javascript\" src=\"https://ajax.googleapis.com/ajax/libs/jqueryui/1.10.0/jquery-ui.min.js\"></script>\n"
				+ "<script type=\"text/javascript\">\n" + "\t\n" + "\t$(document).ready(function(){\n"
				+ "\t\t$(\"#detail\").hide();\t\t\n" + "\t\t\n" + "\t$(\".summary\").click(function(){\n"
				+ "\t\t$(\"#summary\").fadeIn(200);\n" + "\t\t$(\"#detail\").fadeOut(300);\n"
				+ "\t\t$(\"#detail\").reset();\n" + "\t\t});\n" + "\t\n" + "\t$(\".detail\").click(function(){\n"
				+ "\t\t$(\"#summary\").fadeOut(300);\n" + "\t\t$(\"#detail\").fadeIn(400);\n" + "\t\t});\t\t\n"
				+ "\t\t\n" + "\t$(\".suitenametag li\").on('click', function(){\n"
				+ "\t\t$(\".suitenametag li.active\").removeClass('active');\n" + "\t\t$(this).addClass('active');\n"
				+ "\t\t$(\"#testMethods\").fadeOut(200);\n" + "\t\t$(\"#testSteps\").fadeOut(200);\n"
				+ "\t\tvar paneltoShow = $(this).attr('rel');\n"
				+ "\t\t$(\"#tests .panel.active\").fadeOut(100, showNext);\n" + "\t\tfunction showNext(){\n"
				+ "\t\t\t$(this).removeClass('active');\n" + "\t\t\t$('#'+paneltoShow).fadeIn(200, function(){\n"
				+ "\t\t\t\t$(this).addClass('active');\n" + "\t\t\t});\n" + "\t\t}\n" + "\t});\n" + "\t\n"
				+ "\t$(\"#tests div li\").on('click', function(){\n"
				+ "\t\t$(\"#tests div li.active\").removeClass('active');\n" + "\t\t$(this).addClass('active');\n"
				+ "\t\t$(\"#testMethods\").fadeIn(400);\n" + "\t\t$(\"#testSteps\").fadeOut(200);\n"
				+ "\t\tvar testMethodtoShow = $(this).attr(\"rel\");\n"
				+ "\t\t$(\"#testMethods table .methodpanel.active\").fadeOut(10,function(){\n"
				+ "\t\t\t$(this).removeClass('active');\n" + "\t\t\t$(\"#\"+testMethodtoShow).fadeIn(100, function(){\n"
				+ "\t\t\t\t$(this).addClass('active');\n" + "\t\t\t});\n" + "\t\t});\n" + "\t\t\n" + "\t});\n" + "\t\n"
				+ "\t$('#testMethods table tbody tr td a').on('click',function(){\n"
				+ "\t\t$(\"#testMethods table tbody tr td.active\").removeClass('active');\n"
				+ "\t\t$(this).addClass('active');\n" + "\t\t$(\"#testSteps\").fadeIn(200);\n"
				+ "\t\tvar testStepsToShow = $(this).attr('rel');\n"
				+ "\t\t$(\"#testSteps table .steppanel.active\").fadeOut(100, function(){\n"
				+ "\t\t\t$(this).removeClass('active');\n" + "\t\t\t$(\"#\"+testStepsToShow).fadeIn(100,function(){\n"
				+ "\t\t\t\t$(this).addClass('active');\n" + "\t\t\t});\n" + "\t\t});\n" + "\t});\n" + "});\n"
				+ "\t\t\t\n" + "\n" + "\t\n" + "</script>\n" + "\n" + "\n" + "<style type=\"text/css\">\n" + " *{\n"
				+ "\t margin: 0;\n" + "\t padding: 0;\n" + "    -webkit-box-sizing: border-box;\n"
				+ "    -moz-box-sizing: border-box;\n" + "\tbox-sizing: border-box;\n" + "}\n" + "table,th,td{\n"
				+ "border:none;\n" + "}\n" + "table{\n" + "width:100%;\n" + "margin:0 auto;\n" + "display:table;\n"
				+ "}\n" + "th, td {\n" + "\tborder-bottom: 1px solid #ccc !important;\n" + "\t\n" + "}\n" + "th {\n"
				+ "\tcolor: white !important;\n" + "\tfont-family: Roboto, Arial;\n" + "\tfont-size: 12px;\n"
				+ "\tfont-weight: 500 !important;\n" + "\tpadding: 7px 10px;\n" + "\ttext-transform: uppercase;\n"
				+ "\tbackground-color:#435255;\n" + "}\n" + "\n" + "#testsummery{\n" + "\tmargin: 0 auto;\n"
				+ "\twidth:99%;\n" + "\tmargin-bottom:20px;\n" + "\tmargin-top:10px;\n" + "}\n" + "td {\n"
				+ "text-align:center;\n" + "\tfont-size: 15px;\n" + "\tfont-weight: 400;\n" + "\tpadding: 7px 10px;\n"
				+ "\tcolor:#435255 !important;\n" + "}\n" + "\n" + "#header{\n" + "\tdisplay:box;\n" + "\twidth:100%;\n"
				+ "\theight:70px;\n" + "\tmargin:0px auto;\n" + "\tbackground-color:#d84e55;\n" + "}\n"
				+ "#header>#CompanyLogo img{\n" + "\tdisplay: block;\n" + "\tmargin-left:20px;\n" + "\tpadding:12px;\n"
				+ "    float:left;\n" + "    overflow: hidden;\n" + "}\n" + "#header>#title{\n" + "\tfloat:right;\n"
				+ "\ttext-align:right;\n" + "\theight:0 auto;\n" + "\tmargin-right:50px;\n" + "\tfont-size:60dp;\n"
				+ "\tfont-family:Georgia;\n" + "\tdisplay:block;\n" + "\tcolor:white;\n" + "\tpadding:16px;\n" + "}\n"
				+ "\n" + "#main{\n" + "\tmargin-top:10px;\n" + "\twidth:100%;\n" + "}\n" + "#nav{\n" + "\tfloat:left;\n"
				+ "\tmargin-left:0;\n" + "\twidth:10%;\n" + "\theight:200px;\n" + "}\n" + "#nav a{\n"
				+ "\tdisplay:block;\n" + "\theight:50%;\n" + "\tpadding-top:25%;\n" + "\tvertical-align:middle;\n"
				+ "\ttext-align:center;\n" + "\ttext-transform:uppercase;\n" + "\ttext-decoration:none;\n" + "}\n"
				+ "\n" + "\n" + "#summary{\n" + "position:absolute;\n" + "\theight: 85%;\n" + "\tmargin-left:10%;\n"
				+ "\twidth:90%;\n" + "}\n" + "\n" + "#summary>#summeryBox{\n" + "\tdisplay:block;\n"
				+ "\theight:120px;\n" + "\twidth: 100%;\n" + "}\n" + "#summary>#summeryBox>#box{\n"
				+ "    float: left;\n" + "\tposition: relative;\n" + "\twidth: 15%;\n" + "    height: 100px;\n"
				+ "    border: 1px solid #ddd;\n" + "\tdisplay:block;\n" + "\tpadding:12px 15px;\n"
				+ "  \tmargin-left:0;\n" + "\tmargin:0.8337%;\n" + "\tmargin-top:0;\n" + "}\n"
				+ "#summary>#summeryBox>#box>#boxname{\n" + "    position: absolute;\n" + "\tfont-size:17px;\n"
				+ "    top: 0px;\n" + "    left:10px;\n" + "\tpadding:10px;\n" + "} \n"
				+ "#summary>#summeryBox>#box>#value {\n" + "    position: absolute;\n" + "\tfont-size:20px;\n"
				+ "    bottom: 5px;\n" + "    right:10px;\n" + "\tpadding:10px;\n" + "} \n" + ".environment{\n"
				+ "\tdisplay:box;\n" + "\tmargin-top:15px;\n" + "\tmargin-left:9px;\n" + "\twidth:50%;\n" + "}\n"
				+ "#detail{\n" + "\theight: 85%;\n" + "\tmargin-left:10%;\n" + "\twidth:90%;\n" + "}\n"
				+ "#detail>.suitenametag{\n" + "\tdisplay:block;\n" + "\tfloat:left;\n" + "\twidth: 100%;\n"
				+ "\tborder-bottom:2px solid;\n" + "}\n" + "#TestTitle,.suitenametag span{\n"
				+ "\tfloat:left;\ncolor: white;" + "\tdisplay:block;margin-top:5px;\n" + "\tpadding-left:15px;\n"
				+ "\tpadding-right:15px;\n" + "\tpadding-top:7px;\n" + "\tpadding-bottom:7px;\t\n"
				+ "\tbackground-color:#d84e55;\n" + "\t\n" + "}\n" + "#detail>.suitenametag li{\n" + "\tfloat: left;\n"
				+ "    border: 1px solid #ddd;\n" + "\tdisplay:block;\n" + "\tpadding:7px 20px;\n"
				+ "  \tmargin-left:0;\n" + "\tmargin:0.8337%;\n" + "\tmargin-top:0;\n" + "\ttext-decoration:none;\n"
				+ "\tborder-radius:10px;\n" + "}\n" + "li.active{\n" + "\tcolor:white;\n"
				+ "\tbackground-color:#435255;\n" + "}\n" + "\n" + "#tests div{\n" + "\tfloat:left;\n"
				+ "\tmargin: 0 auto;\n" + "\twidth:100%;\n" + "\tmargin-bottom:20px;\n" + "\tmargin-top:7px;\n"
				+ "\tborder-bottom:2px solid;\n" + "}\n" + "#tests div > li{\n" + "float:left;\n"
				+ "    border: 1px solid #ddd;\n" + "\tdisplay:block;\n" + "\tpadding:7px 10px;padding-right:5px;\n"
				+ "  \tmargin-left:0;\n" + "\tmargin:0.8337%;\n" + "\tmargin-top:5px;\n" + "\ttext-decoration:none;\n"
				+ "\tborder-radius:10px;\n" + "}\n" + "#testMethods{\n" + "\tdisplay:none;\n" + "\tfloat:left;\n"
				+ "\tmargin-top:10px;\n" + "\tmargin-left:9px;\n" + "\twidth:35%;\n" + "}\n"
				+ "#testMethods tbody td{\n" + "\ttext-align:left;\n" + "}\n"
				+ ".steppanel.active,.methodpanel.active,.panel.active{\n" + "\tdisplay:block;\n" + "}\n"
				+ ".steppanel,.methodpanel,.panel{\n" + "\tdisplay:none;\n" + "}\n" + "\n" + "#testSteps{\n"
				+ "\tdisplay:none;\n" + "\tfloat:right;\n" + "\tmargin-top:10px;\n" + "\tmargin-left:9px;\n"
				+ "\twidth:62%;\n" + "}\n" + "#testSteps tbody td{\n" + "\ttext-align:left;}\n"
				+ "td a{width:100%; display:block }li img{float:right;margin-right:0;margin-left:10px;margin-top:3px;}\n"
				+ "</style>\n" + "</head>\n" + "<body>\n" + "    <div id=\"header\">\n" + "\t\t<div id=\"title\">\n"
				+ "\t\t\t<h1>Test Automation Report</h1>\n" + "\t\t</div>\n" + "\t\t<div id=\"CompanyLogo\">\n"
				+ "\t\t\t<img src=\"CompanyLogo.png\" alt=\"Company logo\" />\n"
				+ "\t\t</div>\n" + "\t</div>\n" + "\t<div id=\"main\">\n" + "\t\t<div id=\"nav\">\n"
				+ "\t\t\t<a href=\"#summary\" class=\"summary\">summary</a>\n"
				+ "\t\t\t<a href=\"#detail\" class=\"detail\">detail</a>\n" + "\t\t</div>");
	}

	/** Finishes HTML stream */
	protected void endHtml(PrintWriter out) {
		out.println("</body></html>");
	}

	// ~ Inner Classes --------------------------------------------------------
	/** Arranges methods by classname and method name */
	private class TestSorter implements Comparator<IInvokedMethod> {
		// ~ Methods
		// -------------------------------------------------------------

		/** Arranges methods by classname and method name */
		public int compare(IInvokedMethod o1, IInvokedMethod o2) {
			
			return (int) (o1.getDate() - o2.getDate());
			
		}
	}
}
