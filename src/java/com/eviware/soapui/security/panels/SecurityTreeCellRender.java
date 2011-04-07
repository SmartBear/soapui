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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;

import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStep;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.security.SecurityCheck;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.security.SecurityTest;
import com.eviware.soapui.security.check.AbstractSecurityCheck;
import com.eviware.soapui.security.support.ProgressBarSecurityCheckAdapter;
import com.eviware.soapui.security.support.ProgressBarSecurityTestStepAdapter;
import com.eviware.soapui.support.UISupport;

@SuppressWarnings( "serial" )
public class SecurityTreeCellRender implements TreeCellRenderer
{

	Map<DefaultMutableTreeNode, Component> componentTree = new HashMap<DefaultMutableTreeNode, Component>();
	private JTree tree;
	Color selected = new Color( 215, 215, 215 );
	Color unselected = new Color( 228, 228, 228 );

	@Override
	public Component getTreeCellRendererComponent( JTree arg0, Object node, boolean sel, boolean exp, boolean leaf,
			int arg5, boolean arg6 )
	{

		Component result = null;
		this.tree = arg0;

		if( componentTree.containsKey( node ) )
		{
			// TODO: update if needed.
			result = componentTree.get( node );

			( ( CustomTreeNode )result ).setExpandedIcon( exp );
			( ( CustomTreeNode )result ).updateLabel();
			( ( CustomTreeNode )result ).setSelected( sel );
		}
		else
		{
			if( node instanceof TestStepNode )
				result = getTreeCellRendererTestNode( arg0, ( TestStepNode )node, sel, exp, leaf, arg5, arg6 );
			if( node instanceof SecurityCheckNode )
				result = getTreeCellRendererSecurityCheckNode( arg0, ( SecurityCheckNode )node, sel, exp, leaf, arg5, arg6 );

			componentTree.put( ( DefaultMutableTreeNode )node, result );
		}
		return result;
	}

	private Component getTreeCellRendererSecurityCheckNode( JTree arg0, SecurityCheckNode node, boolean sel,
			boolean arg3, boolean arg4, int arg5, boolean arg6 )
	{
		return new SecurityCheckCellRender( arg0, node, sel, arg3, arg4, arg5, arg6 );
	}

	private Component getTreeCellRendererTestNode( JTree arg0, TestStepNode node, boolean sel, boolean arg3,
			boolean arg4, int arg5, boolean arg6 )
	{
		return new TestStepCellRender( arg0, node, sel, arg3, arg4, arg5, arg6 );
	}

	public class TestStepCellRender extends JPanel implements PropertyChangeListener, CustomTreeNode, ReleasableNode
	{
		private WsdlTestStep testStep;
		private JProgressBar progressBar;
		private JLabel label;
		private ProgressBarSecurityTestStepAdapter progressBarAdapter;
		private SecurityTest securityTest;
		private Icon collapsed = UISupport.createImageIcon( "/plus.gif" );
		private Icon expanded = UISupport.createImageIcon( "/minus.gif" );
		private JButton expandCollapseBtn;
		private DefaultMutableTreeNode node;
		private JPanel innerLeftPanel;
		private JPanel progressPanel;
		private JLabel cntLabel;

		public TestStepCellRender( final JTree tree, TestStepNode node, boolean sel, boolean exp, boolean leaf, int arg5,
				boolean arg6 )
		{
			super( new BorderLayout() );

			this.node = node;
			this.testStep = ( WsdlTestStep )node.getTestStep();
			securityTest = ( ( SecurityTreeRootNode )node.getParent() ).getSecurityTest();
			if( AbstractSecurityCheck.isSecurable( testStep ) )
			{
				if( securityTest.getSecurityChecksMap().get( testStep.getId() ) != null )
				{
					String labelText = securityTest.getSecurityChecksMap().get( testStep.getId() ).size() == 1 ? securityTest
							.getSecurityChecksMap().get( testStep.getId() ).size()
							+ " check)"
							: securityTest.getSecurityChecksMap().get( testStep.getId() ).size() + " checks)";
					label = new JLabel( testStep.getLabel() + " (" + labelText, SwingConstants.LEFT );
				}
				else
				{
					label = new JLabel( testStep.getLabel() + " (0 checks)", SwingConstants.LEFT );
				}
			}
			else
				label = new JLabel( testStep.getLabel(), SwingConstants.LEFT );
			label.setIcon( testStep.getIcon() );
			label.setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ) );
			label.setEnabled( !testStep.isDisabled() );
			testStep.addPropertyChangeListener( TestStep.ICON_PROPERTY, TestStepCellRender.this );

			innerLeftPanel = new JPanel( new BorderLayout() );

			if( exp )
				expandCollapseBtn = new JButton( expanded );
			else
				expandCollapseBtn = new JButton( collapsed );

			expandCollapseBtn.setBorder( null );
			expandCollapseBtn.setEnabled( false );

			if( securityTest.getSecurityChecksMap().get( testStep.getId() ) == null
					|| securityTest.getSecurityChecksMap().get( testStep.getId() ).size() == 0 )
				expandCollapseBtn.setVisible( false );
			else
				expandCollapseBtn.setVisible( true );

			innerLeftPanel.add( expandCollapseBtn, BorderLayout.WEST );

			if( AbstractSecurityCheck.isSecurable( testStep ) )
			{
				progressBar = new JProgressBar();

				progressPanel = new JPanel( new FlowLayout( FlowLayout.LEFT, 10, 0 ) );

				progressBar.setValue( 0 );
				progressBar.setStringPainted( true );
				progressBar.setString( "" );
				progressBar.setIndeterminate( false );

				progressBar.setBorder( BorderFactory.createMatteBorder( 0, 0, 1, 1, Color.LIGHT_GRAY ) );

				progressPanel.setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 0 ) );
				progressPanel.add( progressBar );

				progressBar.setMinimumSize( new Dimension( 0, 200 ) );
				progressBar.setBackground( Color.WHITE );
				progressBar.setInheritsPopupMenu( true );

				cntLabel = new JLabel( "" );
				cntLabel.setForeground( Color.white );
				cntLabel.setBackground( selected );
				cntLabel.setBorder( BorderFactory.createLineBorder( Color.LIGHT_GRAY ) );

				progressPanel.add( cntLabel );
				add( progressPanel, BorderLayout.LINE_END );
				expandCollapseBtn.setVisible( true );

				innerLeftPanel.setBorder( BorderFactory.createEmptyBorder( 0, 5, 0, 0 ) );
			}
			else
			{
				expandCollapseBtn.setVisible( false );
				innerLeftPanel.setBorder( BorderFactory.createEmptyBorder( 0, 21, 0, 0 ) );
			}
			innerLeftPanel.add( label, BorderLayout.CENTER );
			add( innerLeftPanel, BorderLayout.LINE_START );

			setSelected( sel );
			setBorder( BorderFactory.createMatteBorder( 0, 0, 1, 0, Color.black ) );
			progressBarAdapter = new ProgressBarSecurityTestStepAdapter( tree, node, progressBar, securityTest, testStep,
					cntLabel );
		}

		public void reset()
		{
			progressBar.setValue( 0 );
			progressBar.setString( "" );
		}

		public void addNotify()
		{
			super.addNotify();
			if( progressBar != null )
			{
				// progressBarAdapter = new ProgressBarSecurityTestStepAdapter(
				// tree, node, progressBar, securityTest,
				// testStep, cntLabel );
			}
		}

		public void removeNotify()
		{
			super.removeNotify();
			if( progressBarAdapter != null )
			{
				// progressBarAdapter.release();
				//
				// progressBarAdapter = null;
			}
		}

		public void setSelected( boolean sel )
		{
			if( sel )
			{
				this.setBackground( selected );
				this.label.setBackground( selected );
				this.innerLeftPanel.setBackground( selected );
				expandCollapseBtn.setBackground( selected );
				if( progressPanel != null )
					progressPanel.setBackground( selected );

			}
			else
			{
				this.setBackground( unselected );
				this.label.setBackground( unselected );
				this.innerLeftPanel.setBackground( unselected );
				expandCollapseBtn.setBackground( unselected );
				if( progressPanel != null )
					progressPanel.setBackground( unselected );
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

		@Override
		public void propertyChange( PropertyChangeEvent arg0 )
		{
			label.setIcon( testStep.getIcon() );
			( ( DefaultTreeModel )tree.getModel() ).nodeChanged( node );
		}

		@Override
		public void setExpandedIcon( boolean exp )
		{
			if( securityTest.getSecurityChecksMap().get( testStep.getId() ) == null
					|| securityTest.getSecurityChecksMap().get( testStep.getId() ).size() == 0 )
			{
				expandCollapseBtn.setVisible( false );
				innerLeftPanel.setBorder( BorderFactory.createEmptyBorder( 0, 21, 0, 0 ) );
			}
			else
			{
				expandCollapseBtn.setVisible( true );
				innerLeftPanel.setBorder( BorderFactory.createEmptyBorder( 0, 5, 0, 0 ) );
			}
			if( exp )
				expandCollapseBtn.setIcon( expanded );
			else
				expandCollapseBtn.setIcon( collapsed );
		}

		@Override
		public void updateLabel()
		{
			if( AbstractSecurityCheck.isSecurable( testStep ) )
			{
				if( securityTest.getSecurityChecksMap().get( testStep.getId() ) != null )
				{
					String labelText = securityTest.getSecurityChecksMap().get( testStep.getId() ).size() == 1 ? securityTest
							.getSecurityChecksMap().get( testStep.getId() ).size()
							+ " check)"
							: securityTest.getSecurityChecksMap().get( testStep.getId() ).size() + " checks)";
					label.setText( testStep.getLabel() + " (" + labelText );
				}
				else
				{
					label.setText( testStep.getLabel() + " (0 checks)" );
				}
			}
		}

		public boolean isOnExpandButton( int x, int y )
		{
			y = y - 30 * ( tree.getRowForLocation( x, y ) );
			if( ( 5 <= x ) && ( 20 >= x ) && ( 5 <= y ) && ( 20 >= y ) )
				return true;
			return false;
		}

		public void release()
		{
			progressBarAdapter.release();
			testStep = null;
			securityTest = null;
		}

	}

	public class SecurityCheckCellRender extends JPanel implements PropertyChangeListener, CustomTreeNode,
			ReleasableNode
	{
		private SecurityCheck securityCheck;
		private JProgressBar progressBar;
		private JLabel label;
		private ProgressBarSecurityCheckAdapter progressBarAdapter;
		private JPanel progressPanel;
		private JLabel cntLabel;
		private SecurityCheckNode node;

		public SecurityCheckCellRender( JTree tree, SecurityCheckNode node, boolean sel, boolean arg3, boolean arg4,
				int arg5, boolean arg6 )
		{
			super( new BorderLayout() );

			this.node = node;
			this.securityCheck = ( SecurityCheck )node.getSecurityCheck();
			label = new JLabel( securityCheck.getName(), SwingConstants.LEFT );
			label.setIcon( UISupport.createImageIcon( "/securityTest.png" ) );
			label.setBorder( BorderFactory.createEmptyBorder( 5, 45, 5, 5 ) );
			label.setEnabled( !securityCheck.isDisabled() );
			add( label, BorderLayout.LINE_START );

			progressBar = new JProgressBar();
			progressPanel = new JPanel( new FlowLayout( FlowLayout.LEFT, 10, 0 ) );

			progressBar.setValue( 0 );
			progressBar.setStringPainted( true );
			progressBar.setString( "" );
			progressBar.setIndeterminate( false );

			progressBar.setBorder( BorderFactory.createMatteBorder( 0, 0, 1, 1, Color.LIGHT_GRAY ) );

			progressPanel.setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 0 ) );
			progressPanel.add( progressBar );

			progressBar.setMinimumSize( new Dimension( 0, 200 ) );
			progressBar.setBackground( Color.WHITE );
			progressBar.setInheritsPopupMenu( true );

			cntLabel = new JLabel( "" );
			cntLabel.setForeground( Color.white );
			cntLabel.setBackground( selected );
			cntLabel.setBorder( BorderFactory.createLineBorder( Color.LIGHT_GRAY ) );

			progressPanel.add( cntLabel );
			add( progressPanel, BorderLayout.LINE_END );

			setSelected( sel );
			setBorder( BorderFactory.createMatteBorder( 0, 0, 1, 0, Color.black ) );

			progressBarAdapter = new ProgressBarSecurityCheckAdapter( tree, this.node, progressBar, securityCheck,
					( SecurityTest )( ( AbstractSecurityCheck )securityCheck ).getParent(), cntLabel );

		}

		public void release()
		{
			progressBarAdapter.release();
			securityCheck = null;
		}

		public void reset()
		{
			progressBar.setValue( 0 );
			progressBar.setString( "" );
		}

		public void setSelected( boolean sel )
		{
			if( sel )
			{
				this.setBackground( selected );
				this.label.setBackground( selected );
				progressPanel.setBackground( selected );
			}
			else
			{
				this.setBackground( unselected );
				this.label.setBackground( unselected );
				progressPanel.setBackground( unselected );
			}
		}

		@Override
		public void propertyChange( PropertyChangeEvent arg0 )
		{
			// label.setIcon( testStep.getIcon() );
		}

		@Override
		public void setExpandedIcon( boolean exp )
		{
			// TODO Auto-generated method stub

		}

		@Override
		public void updateLabel()
		{
			// TODO Auto-generated method stub

		}

	}

	public void remove( DefaultMutableTreeNode node )
	{
		Component component = componentTree.get( node );
		if( component instanceof ReleasableNode )
		{
			( ( ReleasableNode )component ).release();
		}
		componentTree.remove( node );
	}

	public boolean isOn( TestStepNode node, int x, int y )
	{
		TestStepCellRender component = ( TestStepCellRender )componentTree.get( node );
		return component.isOnExpandButton( x, y );
	}

	public void release()
	{
		for( DefaultMutableTreeNode key : componentTree.keySet() )
			if( key instanceof ReleasableNode )
			{
				( ( ReleasableNode )componentTree.get( key ) ).release();
			}
		componentTree.clear();
	}
}
