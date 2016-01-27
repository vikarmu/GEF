/*******************************************************************************
 * Copyright (c) 2016 itemis AG and others.
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.gef4.geometry.planar.BezierCurve;
import org.eclipse.gef4.mvc.behaviors.IBehavior;
import org.eclipse.gef4.mvc.fx.parts.FXDefaultHoverHandlePartFactory;
import org.eclipse.gef4.mvc.parts.IHandlePart;
import org.eclipse.gef4.mvc.parts.IVisualPart;
import org.eclipse.gef4.zest.fx.models.HidingModel;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;

import javafx.scene.Node;

/**
 * @author mwienand
 *
 */
public class HoverHandlePartFactory extends FXDefaultHoverHandlePartFactory {

	@Inject
	private Injector injector;

	@Override
	protected List<IHandlePart<Node, ? extends Node>> createHoverHandlePartsForPolygonalOutline(
			IVisualPart<Node, ? extends Node> target, IBehavior<Node> contextBehavior, Map<Object, Object> contextMap,
			Provider<BezierCurve[]> segmentsProvider) {
		List<IHandlePart<Node, ? extends Node>> handleParts = new ArrayList<>();
		if (target instanceof NodeContentPart) {
			// create prune handle at first vertex
			HideHoverHandlePart hidePart = injector.getInstance(HideHoverHandlePart.class);
			hidePart.setSegmentsProvider(segmentsProvider);
			hidePart.setSegmentIndex(0);
			hidePart.setSegmentParameter(0);
			handleParts.add(hidePart);

			// create expand handle at second vertex but check if we have pruned
			// neighbors, first
			HidingModel hidingModel = target.getRoot().getViewer().getAdapter(HidingModel.class);
			if (hidingModel.hasHiddenNeighbors((NodeContentPart) target)) {
				ShowHiddenNeighborsHoverHandlePart showPart = injector
						.getInstance(ShowHiddenNeighborsHoverHandlePart.class);
				showPart.setSegmentsProvider(segmentsProvider);
				showPart.setSegmentIndex(1);
				showPart.setSegmentParameter(0);
				handleParts.add(showPart);
			}
		}
		return handleParts;
	}

	@Override
	protected List<IHandlePart<Node, ? extends Node>> createHoverHandlePartsForRectangularOutline(
			IVisualPart<Node, ? extends Node> target, IBehavior<Node> contextBehavior, Map<Object, Object> contextMap,
			Provider<BezierCurve[]> segmentsProvider) {
		return createHoverHandlePartsForPolygonalOutline(target, contextBehavior, contextMap, segmentsProvider);
	}

}
