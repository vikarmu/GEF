/*******************************************************************************
 * Copyright (c) 2014 itemis AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthias Wienand (itemis AG) - initial API and implementation
 *
 *******************************************************************************/
package org.eclipse.gef4.mvc.fx.tools;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;

import org.eclipse.gef4.fx.nodes.FXUtils;
import org.eclipse.gef4.mvc.fx.policies.AbstractFXHoverPolicy;
import org.eclipse.gef4.mvc.fx.viewer.FXViewer;
import org.eclipse.gef4.mvc.parts.IVisualPart;
import org.eclipse.gef4.mvc.tools.AbstractTool;
import org.eclipse.gef4.mvc.viewer.IViewer;

public class FXHoverTool extends AbstractTool<Node> {

	public static final Class<AbstractFXHoverPolicy> TOOL_POLICY_KEY = AbstractFXHoverPolicy.class;

	private final EventHandler<MouseEvent> hoverFilter = new EventHandler<MouseEvent>() {
		protected Collection<? extends AbstractFXHoverPolicy> getTargetPolicies(
				MouseEvent event) {
			EventTarget target = event.getTarget();
			if (!(target instanceof Node)) {
				return Collections.emptyList();
			}

			Scene scene = ((Node) target).getScene();
			if (scene == null) {
				return Collections.emptyList();
			}

			// pick target nodes
			List<Node> targetNodes = FXUtils.getNodesAt(scene.getRoot(),
					event.getSceneX(), event.getSceneY());

			IVisualPart<Node, ? extends Node> targetPart = null;
			outer: for (int i = 0; i < targetNodes.size(); i++) {
				Node n = targetNodes.get(i);
				for (IViewer<Node> viewer : getDomain().getViewers().values()) {
					if (viewer instanceof FXViewer) {
						if (((FXViewer) viewer).getScene() == scene) {
							IVisualPart<Node, ? extends Node> part = ((FXViewer) viewer)
									.getVisualPartMap().get(n);
							if (part != null) {
								targetPart = part;
								break outer;
							}
						}
					}
				}
			}

			if (targetPart == null) {
				return Collections.emptyList();
			}

			Collection<? extends AbstractFXHoverPolicy> policies = getHoverPolicies(targetPart);
			return policies;
		}

		@Override
		public void handle(MouseEvent event) {
			if (!event.getEventType().equals(MouseEvent.MOUSE_MOVED)
					&& !event.getEventType().equals(MouseEvent.MOUSE_DRAGGED)) {
				return;
			}

			Collection<? extends AbstractFXHoverPolicy> policies = getTargetPolicies(event);
			for (AbstractFXHoverPolicy policy : policies) {
				policy.hover(event);
			}
		}
	};

	protected Collection<? extends AbstractFXHoverPolicy> getHoverPolicies(
			IVisualPart<Node, ? extends Node> targetPart) {
		return targetPart.<AbstractFXHoverPolicy> getAdapters(TOOL_POLICY_KEY)
				.values();
	}

	@Override
	protected void registerListeners() {
		for (IViewer<Node> viewer : getDomain().getViewers().values()) {
			viewer.getRootPart().getVisual().getScene()
					.addEventFilter(MouseEvent.ANY, hoverFilter);
		}
	}

	@Override
	protected void unregisterListeners() {
		for (IViewer<Node> viewer : getDomain().getViewers().values()) {
			viewer.getRootPart().getVisual().getScene()
					.removeEventFilter(MouseEvent.ANY, hoverFilter);
		}
	}

}
