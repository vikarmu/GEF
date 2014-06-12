/*******************************************************************************
 * Copyright (c) 2014 itemis AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alexander Nyßen (itemis AG) - initial API and implementation
 *
 *******************************************************************************/
package org.eclipse.gef4.mvc.fx.parts;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.layout.Pane;

import org.eclipse.gef4.fx.anchors.IFXAnchor;
import org.eclipse.gef4.mvc.fx.behaviors.FXHoverBehavior;
import org.eclipse.gef4.mvc.fx.behaviors.FXSelectionBehavior;
import org.eclipse.gef4.mvc.fx.policies.FXHoverOnHoverPolicy;
import org.eclipse.gef4.mvc.fx.policies.FXSelectOnClickPolicy;
import org.eclipse.gef4.mvc.fx.tools.FXClickTool;
import org.eclipse.gef4.mvc.fx.tools.FXHoverTool;
import org.eclipse.gef4.mvc.parts.AbstractContentPart;
import org.eclipse.gef4.mvc.parts.IVisualPart;

public abstract class AbstractFXContentPart extends AbstractContentPart<Node> {

	public AbstractFXContentPart() {
		// register (default) interaction policies (which are based on viewer
		// models and do not depend on transaction policies)
		setAdapter(FXClickTool.TOOL_POLICY_KEY, new FXSelectOnClickPolicy());
		setAdapter(FXHoverTool.TOOL_POLICY_KEY, new FXHoverOnHoverPolicy());

		// register behaviors ( (which are based on viewer models)
		setAdapter(FXHoverBehavior.class, new FXHoverBehavior());
		setAdapter(FXSelectionBehavior.class, new FXSelectionBehavior());
	}

	@Override
	protected void addChildVisual(IVisualPart<Node> child, int index) {
		if (getVisual() instanceof Group) {
			((Group) getVisual()).getChildren().add(index, child.getVisual());
		} else if (getVisual() instanceof Pane) {
			((Pane) getVisual()).getChildren().add(index, child.getVisual());
		} else {
			throw new UnsupportedOperationException();
		}
	}

	@Override
	public void attachVisualToAnchorageVisual(IVisualPart<Node> anchorage,
			Node anchorageVisual) {
		// do nothing by default
	}

	@Override
	public void detachVisualFromAnchorageVisual(IVisualPart<Node> anchorage,
			Node anchorageVisual) {
		// do nothing by default
	}

	public IFXAnchor getAnchor(IVisualPart<Node> anchored) {
		// no anchor by default
		return null;
	}

	@Override
	protected void removeChildVisual(IVisualPart<Node> child) {
		if (getVisual() instanceof Group) {
			((Group) getVisual()).getChildren().remove(child.getVisual());
		} else if (getVisual() instanceof Pane) {
			((Pane) getVisual()).getChildren().remove(child.getVisual());
		} else {
			throw new UnsupportedOperationException();
		}
	}

}
