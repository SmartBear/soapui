package com.eviware.soapui.security.ui;

import java.awt.BorderLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JTextField;

import com.eviware.soapui.security.check.LargeAttachmentSecurityCheck;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.components.SimpleForm;

public class LargeAttachmentSecurityCheckConfigPanel extends
		SecurityCheckConfigPanel {

	protected LargeAttachmentSecurityCheck largeAttCheck;

	public LargeAttachmentSecurityCheckConfigPanel(
			LargeAttachmentSecurityCheck secCheck) {
		super(new BorderLayout());
		largeAttCheck = secCheck;

		long numberOfGb = (secCheck.getMaxSize() / (long) Math.pow(1024, 3));
		long temp = secCheck.getMaxSize() - numberOfGb * (long) Math.pow(1024, 3);
		long numberOfMb = (long) (temp / (long) Math.pow(1024, 2));
		temp = temp - numberOfMb * (long) Math.pow(1024, 2);
		long numberOfKb = (long) (temp / 1024);
		long numberOfb = temp - numberOfKb * 1024;
	

		form = new SimpleForm();
		form.addSpace(5);

		JTextField gbField = form.appendTextField("Gb",
				"Maximum Size of the file to send");
		gbField.addKeyListener(new DigitListener());
		gbField.setText(String.valueOf(numberOfGb));
		gbField.setColumns(4);

		JTextField mbField = form.appendTextField("Mb",
				"Maximum Size of the file to send");
		mbField.addKeyListener(new DigitListener());
		mbField.setText(String.valueOf(numberOfMb));
		mbField.setColumns(4);

		JTextField kbField = form.appendTextField("kb",
				"Maximum Size of the file to send");
		kbField.addKeyListener(new DigitListener());
		kbField.setText(String.valueOf(numberOfKb));
		kbField.setColumns(4);

		JTextField bField = form.appendTextField("b",
				"Maximum Size of the file to send");
		bField.addKeyListener(new DigitListener());
		bField.setText(String.valueOf(numberOfb));
		bField.setColumns(4);

		form.appendSeparator();

		add(form.getPanel());
	}

	@Override
	public void save() {
		String gbStr = form.getComponentValue("Gb");
		long numGb = StringUtils.isNullOrEmpty(gbStr) ? 0 : Long
				.valueOf(gbStr);
		String mbStr = form.getComponentValue("Mb");
		long numMb = StringUtils.isNullOrEmpty(mbStr) ? 0 : Long
				.valueOf(mbStr);
		String kbStr = form.getComponentValue("kb");
		long numKb = StringUtils.isNullOrEmpty(kbStr) ? 0 : Long
				.valueOf(kbStr);
		String bStr = form.getComponentValue("b");
		long numB = StringUtils.isNullOrEmpty(bStr) ? 0 : Long.valueOf(bStr);
		long totalBytes = numGb * (long) Math.pow(1024, 3) + numMb
				* (long) Math.pow(1024, 2) + numKb * 1024 + numB;
		largeAttCheck.setMaxSize(totalBytes);


	}

	private class DigitListener implements KeyListener {

		@Override
		public void keyPressed(KeyEvent arg0) {

		}

		@Override
		public void keyReleased(KeyEvent arg0) {

		}

		@Override
		public void keyTyped(KeyEvent ke) {
			char c = ke.getKeyChar();
			if (!Character.isDigit(c))
				ke.consume();
		}
	}

}
