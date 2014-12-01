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

import org.eclipse.gef4.mvc.parts.AbstractContentPart;
import org.eclipse.gef4.mvc.parts.IVisualPart;

public abstract class AbstractFXContentPart<N extends Node> extends
		AbstractContentPart<Node, N> {

	@Override
	protected void addChildVisual(IVisualPart<Node, ? extends Node> child,
			int index) {
		if (getVisual() instanceof Group) {
			((Group) getVisual()).getChildren().add(index, child.getVisual());
		} else if (getVisual() instanceof Pane) {
			((Pane) getVisual()).getChildren().add(index, child.getVisual());
		} else {
			throw new UnsupportedOperationException();
		}
	}

	@Override
	protected void attachToAnchorageVisual(
			IVisualPart<Node, ? extends Node> anchorage, String role) {
		// do nothing by default
	}

	@Override
	protected void detachFromAnchorageVisual(
			IVisualPart<Node, ? extends Node> anchorage, String role) {
		// do nothing by default
	}

	@Override
	protected void removeChildVisual(IVisualPart<Node, ? extends Node> child,
			int index) {
		if (getVisual() instanceof Group) {
			((Group) getVisual()).getChildren().remove(child.getVisual());
		} else if (getVisual() instanceof Pane) {
			((Pane) getVisual()).getChildren().remove(child.getVisual());
		} else {
			throw new UnsupportedOperationException();
		}
	}

}
