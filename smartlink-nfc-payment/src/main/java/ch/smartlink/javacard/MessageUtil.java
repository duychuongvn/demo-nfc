package ch.smartlink.javacard;

import java.math.BigDecimal;

public class MessageUtil {
	public static String getDisplayBalance(BigDecimal balance) {
		return balance.setScale(2).toPlainString();
	}

	public static String formatBalanceToStore(BigDecimal balance) {
		String data = getDisplayBalance(balance);
		return leftZeroPadding(data, 14);
	}

	public static String leftZeroPadding(String value, int len) {
		String data = value;
		while (data.length() < len) {
			data = "0" + data;
		}
		return data;
	}
}
