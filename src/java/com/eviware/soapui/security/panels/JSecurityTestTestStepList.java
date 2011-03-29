/*
 *  soapUI, copyright (C) 2004-2011 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.security.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.dnd.Autoscroll;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.SwingConstants;

import com.eviware.soapui.impl.wsdl.actions.testsuite.AddNewTestCaseAction;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStep;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.security.SecurityCheck;
import com.eviware.soapui.model.support.TestSuiteListenerAdapter;
import com.eviware.soapui.model.testsuite.TestCase;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.model.testsuite.TestSuiteListener;
import com.eviware.soapui.security.SecurityTest;
import com.eviware.soapui.security.check.AbstractSecurityCheck;
import com.eviware.soapui.security.log.JSecurityTestRunLog;
import com.eviware.soapui.security.support.ProgressBarSecurityTestStepAdapter;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.swing.ActionList;
import com.eviware.soapui.support.action.swing.ActionListBuilder;
import com.eviware.soapui.support.action.swing.ActionSupport;
import com.eviware.soapui.support.action.swing.SwingActionDelegate;
import com.eviware.soapui.support.components.JComponentInspector;
import com.eviware.soapui.support.swing.AutoscrollSupport;

/**
 * A panel showing a scrollable list of TestSteps in a SecurityTest.
 * 
 * @author dragica.soldo
 */

@SuppressWarnings( "serial" )
public class JSecurityTestTestStepList extends JPanel
{
	private Map<TestStep, TestStepListEntryPanel> panels = new HashMap<TestStep, TestStepListEntryPanel>();
	private final SecurityTest securityTest;
	private final TestSuiteListener testSuiteListener = new InternalTestSuiteListener();
	private TestStepListEntryPanel selectedTestStep;
	// private JInspectorPanel inspectorPanel;
	private JList securityChecksList;
	JSplitPane splitPane;
	JComponentInspector<JComponent> securityChecksInspector;
	private JComponent secCheckPanel;
	JPanel testStepListPanel;
	JSecurityTestRunLog securityTestLog;
	
	public JSecurityTestTestStepList( SecurityTest securityTest, JSecurityTestRunLog securityTestLog )
	{
		testStepListPanel = new JPanel();
		testStepListPanel.setLayout( new BoxLayout( testStepListPanel, BoxLayout.Y_AXIS ) );
		this.securityTest = securityTest;
		setLayout( new BoxLayout( this, BoxLayout.Y_AXIS ) );

		for( int c = 0; c < securityTest.getTestCase().getTestStepCount(); c++ )
		{
			TestStepListEntryPanel testStepListEntryPanel = createTestStepListPanel( securityTest.getTestCase()
					.getTestStepAt( c ) );
			panels.put( securityTest.getTestCase().getTestStepAt( c ), testStepListEntryPanel );
			testStepListPanel.add( testStepListEntryPanel );
		}
		testStepListPanel.add( Box.createVerticalGlue() );

		secCheckPanel = buildSecurityChecksPanel();
		splitPane = UISupport
				.createVerticalSplit( new JScrollPane( testStepListPanel ), new JScrollPane( secCheckPanel ) );
		splitPane.setPreferredSize( new Dimension( 600, 400 ) );
		splitPane.setDividerLocation( 0.5 );
		splitPane.setResizeWeight( 0.6 );

		add( splitPane, BorderLayout.CENTER );
		securityTest.getTestCase().getTestSuite().addTestSuiteListener( testSuiteListener );
		
		this.securityTestLog = securityTestLog;
	}

	public SecurityCheck getCurrentSecurityCheck()
	{
		int ix = securityChecksList.getSelectedIndex();
		return ix == -1 ? null : securityTest.getTestStepSecurityCheckAt( selectedTestStep.getTestStep().getId(), ix );
	}

	protected JPanel buildSecurityChecksPanel()
	{
		if( selectedTestStep != null && AbstractSecurityCheck.isSecurable( selectedTestStep.getTestStep() ) )
		{
			return new SecurityChecksPanel( selectedTestStep.getTestStep(), securityTest, securityTestLog );
		}
		else
		{
			return new JPanel();
		}
	}

	protected JComponent buildSecurityChecksInspector()
	{
		JPanel p = new JPanel( new BorderLayout() );
		return p;
	}

	public void reset()
	{
		for( TestStepListEntryPanel testStepPanel : panels.values() )
		{
			if( AbstractSecurityCheck.isSecurable( testStepPanel.getTestStep() ) )
			{
				testStepPanel.reset();
			}
		}
	}

	@Override
	public void addNotify()
	{
		super.addNotify();
		securityTest.getTestCase().getTestSuite().addTestSuiteListener( testSuiteListener );
	}

	@Override
	public void removeNotify()
	{
		super.removeNotify();
		securityTest.getTestCase().getTestSuite().removeTestSuiteListener( testSuiteListener );
	}

	private final class InternalTestSuiteListener extends TestSuiteListenerAdapter
	{
		@Override
		public void testStepAdded( TestStep testStep, int index )
		{
			TestStepListEntryPanel testStepListEntry = createTestStepListPanel( testStep );
			panels.put( testStep, testStepListEntry );
			testStepListPanel.add( testStepListEntry, index );
			splitPane.remove( splitPane.getTopComponent() );
			splitPane.setTopComponent( new JScrollPane( testStepListPanel ) );
			revalidate();
			repaint();
		}

		@Override
		public void testStepRemoved( TestStep testStep, int index )
		{
			TestStepListEntryPanel testCaseListPanel = panels.get( testStep );
			if( testCaseListPanel != null )
			{
				remove( testCaseListPanel );
				TestStepListEntryPanel testStepListEntry = panels.remove( testStep );
				testStepListPanel.remove( testStepListEntry );
				testStepListEntry.release();
				splitPane.remove( splitPane.getTopComponent() );
				splitPane.setTopComponent( new JScrollPane( testStepListPanel ) );

				if( secCheckPanel != null )
				{
					if( secCheckPanel instanceof SecurityChecksPanel )
					{
						( ( SecurityChecksPanel )secCheckPanel ).release();
						splitPane.remove( secCheckPanel );
						selectedTestStep = null;
						secCheckPanel = buildSecurityChecksPanel();
						secCheckPanel.revalidate();
						splitPane.setBottomComponent( secCheckPanel );
						splitPane.revalidate();
					}
				}

				revalidate();
				repaint();
			}
		}

		@Override
		public void testStepMoved( TestStep testStep, int index, int offset )
		{
			TestStepListEntryPanel testStepListEntry = panels.get( testStep );
			if( testStepListEntry != null )
			{
				boolean hadFocus = testStepListEntry.hasFocus();

				testStepListPanel.remove( testStepListEntry );
				testStepListPanel.add( testStepListEntry, index + offset );
				splitPane.remove( splitPane.getTopComponent() );
				splitPane.setTopComponent( new JScrollPane( testStepListPanel ) );

				revalidate();
				repaint();

				if( hadFocus )
					testStepListEntry.requestFocus();
			}
		}
	}

	public final class TestStepListEntryPanel extends JPanel implements Autoscroll, PropertyChangeListener
	{
		private final WsdlTestStep testStep;
		private JProgressBar progressBar;
		private JLabel label;
		private ProgressBarSecurityTestStepAdapter progressBarAdapter;
		private TestCasePropertyChangeListener testCasePropertyChangeListener;
		private AutoscrollSupport autoscrollSupport;

		public TestStepListEntryPanel( WsdlTestStep testStep )
		{
			super( new BorderLayout() );

			setFocusable( true );

			this.testStep = testStep;
			autoscrollSupport = new AutoscrollSupport( this );
			label = new JLabel( testStep.getLabel(), SwingConstants.LEFT );
			label.setIcon( testStep.getIcon() );
			label.setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ) );
			label.setInheritsPopupMenu( true );
			label.setEnabled( !testStep.isDisabled() );
			testStep.addPropertyChangeListener( TestStep.ICON_PROPERTY, this );

			add( label, BorderLayout.LINE_START );

			if( AbstractSecurityCheck.isSecurable( testStep ) )
			{
				progressBar = new JProgressBar()
				{
					protected void processMouseEvent( MouseEvent e )
					{
						if( e.getID() == MouseEvent.MOUSE_PRESSED || e.getID() == MouseEvent.MOUSE_RELEASED )
						{
							TestStepListEntryPanel.this.processMouseEvent( translateMouseEvent( e ) );
						}
					}

					protected void processMouseMotionEvent( MouseEvent e )
					{
						TestStepListEntryPanel.this.processMouseMotionEvent( translateMouseEvent( e ) );
					}

					/**
					 * Translates the given mouse event to the enclosing map panel's
					 * coordinate space.
					 */
					private MouseEvent translateMouseEvent( MouseEvent e )
					{
						return new MouseEvent( TestStepListEntryPanel.this, e.getID(), e.getWhen(), e.getModifiers(),
								e.getX() + getX(), e.getY() + getY(), e.getClickCount(), e.isPopupTrigger(), e.getButton() );
					}
				};

				JPanel progressPanel = UISupport.createProgressBarPanel( progressBar, 5, false );

				progressBar.setMinimumSize( new Dimension( 0, 200 ) );
				progressBar.setBackground( Color.WHITE );
				progressBar.setInheritsPopupMenu( true );

				add( progressPanel, BorderLayout.LINE_END );
			}

			testCasePropertyChangeListener = new TestCasePropertyChangeListener();

			// initPopup( testStep );

			addMouseListener( new MouseAdapter()
			{

				public void mouseClicked( MouseEvent e )
				{
					if( e.getClickCount() < 2 )
					{
						if( selectedTestStep != null )
							selectedTestStep.setSelected( false );

						selectedTestStep = TestStepListEntryPanel.this;
						requestFocus();
						splitPane.remove( secCheckPanel );
						secCheckPanel = buildSecurityChecksPanel();
						secCheckPanel.revalidate();
						splitPane.setBottomComponent( secCheckPanel );
						splitPane.revalidate();
						if( AbstractSecurityCheck.isSecurable( selectedTestStep.getTestStep() ) )
						{
							setSelected( true );
						}
					}
					else
					{
						UISupport.selectAndShow( TestStepListEntryPanel.this.testStep );
					}
				}
			} );

			addKeyListener( new TestCaseListPanelKeyHandler() );

			// init border
			setSelected( false );
		}

		public void reset()
		{
			progressBar.setValue( 0 );
			progressBar.setString( "" );
		}

		private void initPopup( WsdlTestStep testStep )
		{
			ActionList actions = ActionListBuilder.buildActions( testStep );
			actions
					.insertAction( SwingActionDelegate.createDelegate( AddNewTestCaseAction.SOAPUI_ACTION_ID, securityTest,
							null, null ), 0 );
			actions.insertAction( ActionSupport.SEPARATOR_ACTION, 1 );

			setComponentPopupMenu( ActionSupport.buildPopup( actions ) );
		}

		public void addNotify()
		{
			super.addNotify();
			testStep.addPropertyChangeListener( testCasePropertyChangeListener );
			if( progressBar != null )
			{
				progressBarAdapter = new ProgressBarSecurityTestStepAdapter( progressBar, securityTest, testStep );
			}
		}

		public void removeNotify()
		{
			super.removeNotify();
			if( progressBarAdapter != null )
			{
				testStep.removePropertyChangeListener( testCasePropertyChangeListener );
				progressBarAdapter.release();

				progressBarAdapter = null;
			}
		}

		public Dimension getMaximumSize()
		{
			Dimension size = super.getMaximumSize();
			size.height = 50;
			return size;
		}

		public void setSelected( boolean selected )
		{
			if( selected )
			{
				setBorder( BorderFactory.createLineBorder( Color.GRAY ) );
			}
			else
			{
				setBorder( BorderFactory.createLineBorder( Color.WHITE ) );
			}
		}

		public boolean isSelected()
		{
			return selectedTestStep != null && selectedTestStep.getTestStep() == testStep;
		}

		private final class TestCasePropertyChangeListener implements PropertyChangeListener
		{
			public void propertyChange( PropertyChangeEvent evt )
			{
				if( evt.getPropertyName().equals( TestCase.LABEL_PROPERTY ) )
				{
					label.setEnabled( !testStep.isDisabled() );
					label.setText( testStep.getLabel() );
				}
				else if( evt.getPropertyName().equals( TestCase.DISABLED_PROPERTY ) )
				{
					initPopup( testStep );
				}
			}
		}

		protected TestStep getTestStep()
		{
			return testStep;
		}

		public ModelItem getModelItem()
		{
			return testStep;
		}

		public void autoscroll( Point pt )
		{
			int ix = getIndexOf( this );
			if( pt.getY() < 12 && ix > 0 )
			{
				Rectangle bounds = JSecurityTestTestStepList.this.getComponent( ix - 1 ).getBounds();
				JSecurityTestTestStepList.this.scrollRectToVisible( bounds );
			}
			else if( pt.getY() > getHeight() - 12 && ix < securityTest.getTestCase().getTestStepCount() - 1 )
			{
				Rectangle bounds = JSecurityTestTestStepList.this.getComponent( ix + 1 ).getBounds();
				JSecurityTestTestStepList.this.scrollRectToVisible( bounds );
			}
		}

		public Insets getAutoscrollInsets()
		{
			return autoscrollSupport.getAutoscrollInsets();
		}

		private final class TestCaseListPanelKeyHandler extends KeyAdapter
		{
			public void keyPressed( KeyEvent e )
			{
				if( e.getKeyChar() == KeyEvent.VK_ENTER )
				{
					UISupport.selectAndShow( testStep );
					e.consume();
				}
				else
				{
					ActionList actions = ActionListBuilder.buildActions( testStep );
					if( actions != null )
						actions.dispatchKeyEvent( e );
				}
			}
		}

		@Override
		public void propertyChange( PropertyChangeEvent evt )
		{
			label.setIcon( testStep.getIcon() );
		}

		public void release()
		{
			testStep.removePropertyChangeListener( this );
		}
	}

	protected int getIndexOf( TestStepListEntryPanel panel )
	{
		return Arrays.asList( getComponents() ).indexOf( panel );
	}

	protected TestStepListEntryPanel createTestStepListPanel( TestStep testStep )
	{
		TestStepListEntryPanel testStepListPanel = new TestStepListEntryPanel( ( WsdlTestStep )testStep );

		return testStepListPanel;
	}

	public void release()
	{
		for( TestStepListEntryPanel testStepPanel : panels.values() )
		{
			testStepPanel.release();
		}
	}

}
