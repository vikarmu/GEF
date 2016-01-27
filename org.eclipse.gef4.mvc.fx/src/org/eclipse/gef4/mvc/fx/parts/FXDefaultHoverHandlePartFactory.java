/*******************************************************************************
 * Copyright (c) 2016 itemis AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthias Wienand (itemis AG) - initial API and implementation
 *
 *******************************************************************************/
package org.eclipse.gef4.mvc.fx.parts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.gef4.common.adapt.AdapterKey;
import org.eclipse.gef4.fx.utils.NodeUtils;
import org.eclipse.gef4.geometry.planar.BezierCurve;
import org.eclipse.gef4.geometry.planar.ICurve;
import org.eclipse.gef4.geometry.planar.IGeometry;
import org.eclipse.gef4.geometry.planar.IShape;
import org.eclipse.gef4.geometry.planar.Rectangle;
import org.eclipse.gef4.mvc.behaviors.HoverBehavior;
import org.eclipse.gef4.mvc.behaviors.IBehavior;
import org.eclipse.gef4.mvc.parts.IHandlePart;
import org.eclipse.gef4.mvc.parts.IHandlePartFactory;
import org.eclipse.gef4.mvc.parts.IVisualPart;

import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;

import javafx.scene.Node;

/**
 *
 * @author mwienand
 *
 */
public class FXDefaultHoverHandlePartFactory
		implements IHandlePartFactory<Node> {

	/**
	 * The role name for the <code>Provider&lt;IGeometry&gt;</code> that will be
	 * used to generate hover handles.
	 */
	public static final String HOVER_HANDLES_GEOMETRY_PROVIDER = "HOVER_HANDLES_GEOMETRY_PROVIDER";

	@Inject
	private Injector injector;

	@Override
	public List<IHandlePart<Node, ? extends Node>> createHandleParts(
			List<? extends IVisualPart<Node, ? extends Node>> targets,
			IBehavior<Node> contextBehavior, Map<Object, Object> contextMap) {
		// check creation context
		if (!(contextBehavior instanceof HoverBehavior)) {
			throw new IllegalArgumentException(
					"The FXDefaultHoverHandlePartFactory can only generate handle parts in the context of a HoverBehavior, but the context behavior is a <"
							+ contextBehavior + ">.");
		}
		// check that we have targets
		if (targets == null || targets.isEmpty()) {
			throw new IllegalArgumentException(
					"Part factory is called without targets.");
		}
		// check that only one part is hovered at a time
		if (targets.size() > 1) {
			throw new IllegalStateException(
					"Cannot create hover handles for more than one target.");
		}

		final IVisualPart<Node, ? extends Node> target = targets.get(0);

		// handle geometry is in target visual local coordinate space.
		@SuppressWarnings("serial")
		final Provider<? extends IGeometry> hoverHandlesGeometryInTargetLocalProvider = target
				.getAdapter(AdapterKey
						.get(new TypeToken<Provider<? extends IGeometry>>() {
						}, HOVER_HANDLES_GEOMETRY_PROVIDER));

		// generate handles from selection handles geometry
		IGeometry hoverHandlesGeometry = (hoverHandlesGeometryInTargetLocalProvider != null)
				? hoverHandlesGeometryInTargetLocalProvider.get() : null;
		if (hoverHandlesGeometry == null) {
			return Collections.emptyList();
		}

		// we will need a provider that returns the geometry in scene
		// coordinates
		final Provider<? extends IGeometry> hoverHandlesGeometryInSceneProvider = new Provider<IGeometry>() {
			@Override
			public IGeometry get() {
				return NodeUtils.localToScene(target.getVisual(),
						hoverHandlesGeometryInTargetLocalProvider.get());
			}
		};

		// the handle parts are located based on the segments of the handle
		// geometry
		Provider<BezierCurve[]> hoverHandlesSegmentsInSceneProvider = new Provider<BezierCurve[]>() {
			@Override
			public BezierCurve[] get() {
				IGeometry handleGeometry = hoverHandlesGeometryInSceneProvider
						.get();
				if (handleGeometry instanceof IShape) {
					List<BezierCurve> segments = new ArrayList<>();
					for (ICurve os : ((IShape) handleGeometry)
							.getOutlineSegments()) {
						segments.addAll(Arrays.asList(os.toBezier()));
					}
					return segments.toArray(new BezierCurve[] {});
				} else if (handleGeometry instanceof ICurve) {
					return ((ICurve) handleGeometry).toBezier();
				} else {
					throw new IllegalStateException(
							"Unable to determine handle position: Expected IShape or ICurve but got: "
									+ handleGeometry);
				}
			}
		};

		if (hoverHandlesGeometry instanceof ICurve) {
			// create curve handles
			return createHoverHandlePartsForCurve(target, contextBehavior,
					contextMap, hoverHandlesSegmentsInSceneProvider);
		} else if (hoverHandlesGeometry instanceof IShape) {
			if (hoverHandlesGeometry instanceof Rectangle) {
				// create box handles
				return createHoverHandlePartsForRectangularOutline(target,
						contextBehavior, contextMap,
						hoverHandlesSegmentsInSceneProvider);
			} else {
				// create segment handles (based on outline)
				return createHoverHandlePartsForPolygonalOutline(target,
						contextBehavior, contextMap,
						hoverHandlesSegmentsInSceneProvider);
			}
		} else {
			throw new IllegalStateException(
					"Unable to generate handles for this handle geometry. Expected ICurve or IShape, but got: "
							+ hoverHandlesGeometry);
		}
	}

	/**
	 * Creates hover handle parts for a handle geometry that is an
	 * {@link ICurve}.
	 *
	 * @param target
	 *            The target {@link IVisualPart} for which handles are to be
	 *            created.
	 * @param contextBehavior
	 *            The context {@link IBehavior} which initiates the creation of
	 *            feedback.
	 * @param contextMap
	 *            A map in which the state-less context {@link IBehavior}) may
	 *            place additional context information for the creation process.
	 *            It may either directly contain additional information needed
	 *            by the {@link IHandlePartFactory}, or may be passed back by
	 *            the {@link IHandlePartFactory} to the calling context
	 *            {@link IBehavior} to query such kind of information (in which
	 *            case it will allow the context {@link IBehavior} to identify
	 *            the creation context).
	 * @param segmentsProvider
	 *            A provider for the segments of the handle geometry for which
	 *            handles are to be created.
	 * @return A list of {@link IHandlePart}s that can be used to manipulate the
	 *         given targets.
	 */
	protected List<IHandlePart<Node, ? extends Node>> createHoverHandlePartsForCurve(
			IVisualPart<Node, ? extends Node> target,
			IBehavior<Node> contextBehavior, Map<Object, Object> contextMap,
			Provider<BezierCurve[]> segmentsProvider) {
		return Collections.emptyList();
	}

	/**
	 * Creates hover handle parts for a handle geometry that is an
	 * {@link IShape} but not a {@link Rectangle}.
	 *
	 * @param target
	 *            The target {@link IVisualPart} for which handles are to be
	 *            created.
	 * @param contextBehavior
	 *            The context {@link IBehavior} which initiates the creation of
	 *            feedback.
	 * @param contextMap
	 *            A map in which the state-less context {@link IBehavior}) may
	 *            place additional context information for the creation process.
	 *            It may either directly contain additional information needed
	 *            by the {@link IHandlePartFactory}, or may be passed back by
	 *            the {@link IHandlePartFactory} to the calling context
	 *            {@link IBehavior} to query such kind of information (in which
	 *            case it will allow the context {@link IBehavior} to identify
	 *            the creation context).
	 * @param segmentsProvider
	 *            A provider for the segments of the handle geometry for which
	 *            handles are to be created.
	 * @return A list of {@link IHandlePart}s that can be used to manipulate the
	 *         given targets.
	 */
	protected List<IHandlePart<Node, ? extends Node>> createHoverHandlePartsForPolygonalOutline(
			IVisualPart<Node, ? extends Node> target,
			IBehavior<Node> contextBehavior, Map<Object, Object> contextMap,
			Provider<BezierCurve[]> segmentsProvider) {
		List<IHandlePart<Node, ? extends Node>> handleParts = new ArrayList<>();
		BezierCurve[] segments = segmentsProvider.get();
		for (int i = 0; i < segments.length; i++) {
			FXCircleSegmentHandlePart part = injector
					.getInstance(FXCircleSegmentHandlePart.class);
			part.setSegmentsProvider(segmentsProvider);
			part.setSegmentIndex(i);
			part.setSegmentParameter(0);
			handleParts.add(part);
		}
		return handleParts;
	}

	/**
	 * Creates hover handle parts for a handle geometry that is a
	 * {@link Rectangle}.
	 *
	 * @param target
	 *            The target {@link IVisualPart} for which handles are to be
	 *            created.
	 * @param contextBehavior
	 *            The context {@link IBehavior} which initiates the creation of
	 *            feedback.
	 * @param contextMap
	 *            A map in which the state-less context {@link IBehavior}) may
	 *            place additional context information for the creation process.
	 *            It may either directly contain additional information needed
	 *            by the {@link IHandlePartFactory}, or may be passed back by
	 *            the {@link IHandlePartFactory} to the calling context
	 *            {@link IBehavior} to query such kind of information (in which
	 *            case it will allow the context {@link IBehavior} to identify
	 *            the creation context).
	 * @param segmentsProvider
	 *            A provider for the segments of the handle geometry for which
	 *            handles are to be created.
	 * @return A list of {@link IHandlePart}s that can be used to manipulate the
	 *         given targets.
	 */
	protected List<IHandlePart<Node, ? extends Node>> createHoverHandlePartsForRectangularOutline(
			IVisualPart<Node, ? extends Node> target,
			IBehavior<Node> contextBehavior, Map<Object, Object> contextMap,
			Provider<BezierCurve[]> segmentsProvider) {
		return Collections.emptyList();
	}

}
