/*******************************************************************************
 * Copyright (c) 2014 itemis AG and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthias Wienand (itemis AG) - initial API & implementation
 *
 *******************************************************************************/
package org.eclipse.gef4.zest.fx.parts;

import java.util.Set;

import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;

import org.eclipse.gef4.mvc.fx.parts.AbstractFXFeedbackPart;
import org.eclipse.gef4.mvc.parts.IVisualPart;
import org.eclipse.gef4.zest.fx.models.SubgraphModel;

// TODO: only applicable for NodeContentPart (anchorage)
public class PrunedNeighborsSubgraphPart extends AbstractFXFeedbackPart<Group> {

	private Circle circle;
	private Text text;

	@Override
	protected void attachToAnchorageVisual(IVisualPart<Node, ? extends Node> anchorage, String role) {
		super.attachToAnchorageVisual(anchorage, role);
		getVisual().visibleProperty().bind(anchorage.getVisual().visibleProperty());
	}

	// TODO: extract visual to its own type
	@Override
	protected Group createVisual() {
		Group visual = new Group();
		visual.setAutoSizeChildren(false);

		circle = new Circle(10);
		// TODO: move to CSS
		circle.setFill(Color.RED);
		circle.setStroke(Color.BLACK);

		text = new Text("0");

		visual.getChildren().addAll(circle, text);
		return visual;
	}

	@Override
	protected void detachFromAnchorageVisual(IVisualPart<Node, ? extends Node> anchorage, String role) {
		super.detachFromAnchorageVisual(anchorage, role);
		getVisual().visibleProperty().unbind();
	}

	@Override
	protected void doRefreshVisual() {
		Set<IVisualPart<Node, ? extends Node>> keySet = getAnchorages().keySet();
		if (keySet.isEmpty()) {
			return;
		}
		IVisualPart<Node, ? extends Node> anchorage = keySet.iterator().next();
		Bounds anchorageLayoutBoundsInLocal = getVisual().sceneToLocal(
				anchorage.getVisual().localToScene(anchorage.getVisual().getLayoutBounds()));

		double x = anchorageLayoutBoundsInLocal.getMaxX();
		double y = anchorageLayoutBoundsInLocal.getMaxY();

		circle.setCenterX(x);
		circle.setCenterY(y);

		SubgraphModel subgraphModel = getViewer().getDomain().getAdapter(SubgraphModel.class);
		Set<NodeContentPart> containedNodes = subgraphModel.getContainedNodes((NodeContentPart) anchorage);
		int count = containedNodes == null ? 0 : containedNodes.size();
		text.setText(Integer.toString(count));

		Bounds textLayoutBounds = text.getLayoutBounds();

		double size = textLayoutBounds.getWidth();
		if (textLayoutBounds.getHeight() > size) {
			size = textLayoutBounds.getHeight();
		}
		circle.setRadius(size / 2);

		text.relocate(x - textLayoutBounds.getWidth() / 2, y - textLayoutBounds.getHeight() / 2);
	}

}
