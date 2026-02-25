package Cloudian.JobPortal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class JobPortalApplication {
	private static void printLine(String borderColor, String content, int width) {
		String RESET = "\u001B[0m";
		// Strip ANSI codes to calculate "real" visible length
		String visibleContent = content.replaceAll("\u001B\\[[;\\d]*m", "");
		int padding = width - visibleContent.length();

		System.out.print(borderColor + "║" + RESET);
		System.out.print(content);
		System.out.print(" ".repeat(Math.max(0, padding)));
		System.out.println(borderColor + "║" + RESET);
	}
	public static void main(String[] args) {
		ConfigurableApplicationContext ctx = SpringApplication.run(JobPortalApplication.class, args); //If it don't throw catch then yessss
		//Check to know if the server is running successfully
		//Colors, don't care it
		String RESET = "\u001B[0m";
		String BOLD = "\u001B[1m";
		String GREEN = "\u001B[32m";
		String CYAN = "\u001B[36m";
		String YELLOW = "\u001B[33m";
		// Define the application context -----
		String title = "🚀 JOB PORTAL SERVER SYSTEM";
		String status = "RUNNING SUCCESSFULLY";
		String endpoint = "http://localhost:8080";
		String developer = "Cloudian";
		String timestamp = java.time.LocalDateTime.now().withNano(0).toString();
		//Log to know the server is running successfully
		System.out.println(CYAN + "╔══════════════════════════════════════════════════════════╗" + RESET);
		printLine(CYAN, BOLD + title, 58);
		System.out.println(CYAN + "╠══════════════════════════════════════════════════════════╣" + RESET);
		printLine(CYAN, " Status      : " + GREEN + status, 58);
		printLine(CYAN, " Endpoint    : " + BOLD + endpoint, 58);
		printLine(CYAN, " Developer   : " + YELLOW + developer, 58);
		printLine(CYAN, " Timestamp   : " + timestamp, 58);
		System.out.println(CYAN + "╚══════════════════════════════════════════════════════════╝" + RESET);
	}

}
