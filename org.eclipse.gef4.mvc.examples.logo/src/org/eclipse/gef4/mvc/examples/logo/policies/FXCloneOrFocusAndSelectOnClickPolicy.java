/*******************************************************************************
 * Copyright (c) 2015 itemis AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthias Wienand (itemis AG) - initial API and implementation
 *
 *******************************************************************************/
package org.eclipse.gef4.mvc.examples.logo.policies;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.gef4.geometry.convert.fx.JavaFX2Geometry;
import org.eclipse.gef4.geometry.planar.AffineTransform;
import org.eclipse.gef4.mvc.fx.policies.FXFocusAndSelectOnClickPolicy;
import org.eclipse.gef4.mvc.fx.policies.FXTransformPolicy;
import org.eclipse.gef4.mvc.models.SelectionModel;
import org.eclipse.gef4.mvc.operations.DeselectOperation;
import org.eclipse.gef4.mvc.parts.IContentPart;
import org.eclipse.gef4.mvc.parts.IRootPart;
import org.eclipse.gef4.mvc.policies.CreationPolicy;
import org.eclipse.gef4.mvc.viewer.IViewer;

import com.google.common.collect.HashMultimap;

import javafx.scene.Node;
import javafx.scene.input.MouseEvent;

public class FXCloneOrFocusAndSelectOnClickPolicy
		extends FXFocusAndSelectOnClickPolicy {

	@Override
	public void click(MouseEvent e) {
		// delegate to super policy in case the clone modifier is not pressed
		if (!isCloneModifierDown(e)) {
			super.click(e);
			return;
		}

		// clone content
		Object cloneContent = getHost()
				.getAdapter(AbstractCloneContentPolicy.class).cloneContent();

		// create the clone content part
		IRootPart<Node, ? extends Node> root = getHost().getRoot();
		CreationPolicy<Node> creationPolicy = root
				.<CreationPolicy<Node>> getAdapter(CreationPolicy.class);
		init(creationPolicy);
		IContentPart<Node, ? extends Node> clonedContentPart = creationPolicy
				.create(cloneContent,
						(IContentPart<Node, ? extends Node>) getHost()
								.getParent(),
						HashMultimap
								.<IContentPart<Node, ? extends Node>, String> create());
		commit(creationPolicy);

		// deselect all but the clone
		IViewer<Node> viewer = getHost().getRoot().getViewer();
		List<? extends IContentPart<Node, ? extends Node>> toBeDeselected = new ArrayList<IContentPart<Node, ? extends Node>>(
				viewer.<SelectionModel<Node>> getAdapter(SelectionModel.class)
						.getSelection());
		toBeDeselected.remove(clonedContentPart);
		viewer.getDomain().execute(new DeselectOperation<Node>(
				getHost().getRoot().getViewer(), toBeDeselected));

		// copy the transformation
		AffineTransform originalTransform = JavaFX2Geometry.toAffineTransform(
				getHost().getAdapter(FXTransformPolicy.TRANSFORM_PROVIDER_KEY)
						.get());
		FXTransformPolicy transformPolicy = clonedContentPart
				.getAdapter(FXTransformPolicy.class);
		init(transformPolicy);
		transformPolicy.setTransform(originalTransform);
		commit(transformPolicy);
	}

	protected boolean isCloneModifierDown(MouseEvent e) {
		return e.isAltDown() || e.isShiftDown();
	}

}