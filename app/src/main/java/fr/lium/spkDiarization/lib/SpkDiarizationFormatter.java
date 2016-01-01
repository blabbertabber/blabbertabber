package fr.lium.spkDiarization.lib;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import edu.cmu.sphinx.util.Utilities;

/**
 * The Class SpkDiarizationFormatter.
 */
public class SpkDiarizationFormatter extends Formatter {

	/*
	 * (non-Javadoc)
	 * @see java.util.logging.Formatter#format(java.util.logging.LogRecord)
	 */
	@Override
	public String format(LogRecord record) {
		Date date = new Date(record.getMillis());
		SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm.SSS");
// SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		StringBuffer sbuf = new StringBuffer();
		sbuf.append(timeFormat.format(date));
		sbuf.append(" ");

		// String names[] = record.getSourceClassName().split("\\.");

		String source = " ";
		if (record.getLevel().intValue() != Level.CONFIG.intValue()) {
			String loggerName = record.getLoggerName();
			if (loggerName != null) {
				String[] strings = loggerName.split("[.]");
				source = strings[strings.length - 1];
			} else {
				source = loggerName;
			}
		}
		sbuf.append(Utilities.pad(source + " ", 15));
		sbuf.append(Utilities.pad(record.getLevel().getName() + " ", 6));
		sbuf.append("| ");
		sbuf.append(record.getMessage());

		if (record.getLevel().intValue() != Level.CONFIG.intValue()) {
			sbuf.append("\t{" + record.getSourceMethodName() + "()" + " / " + record.getThreadID() + "}");
		}
		sbuf.append("\n");
		return sbuf.toString();
	}

}
