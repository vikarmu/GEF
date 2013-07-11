/*******************************************************************************
 * Copyright (c) 2013 itemis AG and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Matthias Wienand (itemis AG) - initial API and implementation
 * 
 *******************************************************************************/
package org.eclipse.gef4.swtfx;

import java.awt.geom.NoninvertibleTransformException;

import org.eclipse.gef4.geometry.planar.AffineTransform;
import org.eclipse.gef4.geometry.planar.Point;
import org.eclipse.gef4.geometry.planar.Rectangle;
import org.eclipse.gef4.swtfx.event.FocusTraversalDispatcher;
import org.eclipse.gef4.swtfx.event.IEventDispatchChain;
import org.eclipse.gef4.swtfx.event.IEventDispatcher;
import org.eclipse.gef4.swtfx.event.IEventTarget;
import org.eclipse.gef4.swtfx.event.MouseTrackDispatcher;

public class NodeUtil {

	/**
	 * Inverts the node's local-to-absolute-transform and uses the resulting
	 * inverted transformation matrix to transform a point from absolute to
	 * local coordinates.
	 * 
	 * @param node
	 * @param absoluteIn
	 * @param localOut
	 */
	public static void absoluteToLocal(INode node, Point absoluteIn,
			Point localOut) {
		AffineTransform tx = node.getLocalToAbsoluteTransform();
		try {
			localOut.setLocation(tx.inverseTransform(absoluteIn));
		} catch (NoninvertibleTransformException e) {
			// TODO: we have to assure that all transformations are invertible
		}
	}

	public static void autosize(INode node) {
		if (node.isResizable()) {
			double width, height;

			Orientation orientation = node.getContentBias();
			switch (orientation) {
			case NONE:
				// no dependence
				width = clamp(node.computePrefWidth(-1),
						node.computeMinWidth(-1), node.computeMaxWidth(-1));
				height = clamp(node.computePrefHeight(-1),
						node.computeMinHeight(-1), node.computeMaxHeight(-1));
				break;
			case HORIZONTAL:
				// height depends on width
				width = clamp(node.computePrefWidth(-1),
						node.computeMinWidth(-1), node.computeMaxWidth(-1));
				height = clamp(node.computePrefHeight(width),
						node.computeMinHeight(width),
						node.computeMaxHeight(width));
				break;
			case VERTICAL:
				// width depends on height
				height = clamp(node.computePrefHeight(-1),
						node.computeMinHeight(-1), node.computeMaxHeight(-1));
				width = clamp(node.computePrefWidth(height),
						node.computeMinWidth(height),
						node.computeMaxWidth(height));
				break;
			default:
				throw new IllegalStateException("Unknown Orientation: "
						+ orientation);
			}

			node.resize(width, height);
		}
	}

	/**
	 * Recursively builds an {@link IEventDispatchChain} for the given
	 * {@link INode target}. The target's {@link IEventDispatcher} is prepended
	 * to the current {@link IEventDispatchChain chain} together with a
	 * {@link MouseTrackDispatcher} for the target.
	 * 
	 * @param target
	 *            the event target
	 * @param tail
	 *            the current {@link IEventDispatchChain}
	 * @return an {@link IEventDispatchChain} for the given target
	 * 
	 * @see IEventTarget#buildEventDispatchChain(IEventDispatchChain)
	 */
	public static IEventDispatchChain buildEventDispatchChain(
			final INode target, final IEventDispatchChain tail) {
		tail.prepend(target.getEventDispatcher());
		tail.prepend(new MouseTrackDispatcher(target));
		tail.prepend(new FocusTraversalDispatcher(target));
		INode next = target.getParentNode();
		if (next != null) {
			return next.buildEventDispatchChain(tail);
		}
		return tail;
	}

	private static double clamp(double value, double min, double max) {
		if (value < min) {
			return min;
		} else if (value > max) {
			return max;
		} else {
			return value;
		}
	}

	/**
	 * Returns the bounds of the passed-in {@link INode} in the coordinate
	 * system of its parent. The boundsInParent result from transforming the
	 * {@link INode#getBoundsInLocal() boundsInLocal} with the
	 * {@link INode#getLocalToParentTransform() localToParentTransform}.
	 * 
	 * @param node
	 * @return the bounds of the passed-in {@link INode} in the coordinate
	 *         system of its parent
	 */
	public static Rectangle getBoundsInParent(INode node) {
		Rectangle boundsInLocal = node.getBoundsInLocal();
		return boundsInLocal.getTransformed(node.getLocalToParentTransform())
				.getBounds();
	}

	/**
	 * Returns an {@link AffineTransform} to transform from the local coordinate
	 * system of the passed-in {@link INode} to the coordinate system of the
	 * screen (i.e. absolute coordinates).
	 * 
	 * @param node
	 * @return an {@link AffineTransform} to transform from the local coordinate
	 *         system of the passed-in {@link INode} to the coordinate system of
	 *         the screen (i.e. absolute coordinates)
	 */
	public static AffineTransform getLocalToAbsoluteTransform(INode node) {
		AffineTransform tx = node.getLocalToParentTransform();
		IParent parent = node.getParentNode();
		if (parent != null) {
			tx.preConcatenate(parent.getLocalToAbsoluteTransform());
		}
		return tx;
	}

	/**
	 * <p>
	 * Returns an {@link AffineTransform} to transform coordinates from the
	 * local coordinate system of the passed-in {@link INode} to the coordinate
	 * system of its parent.
	 * </p>
	 * <p>
	 * The {@link AffineTransform} is constructed by applying the {@link INode}
	 * 's attributes in the following order:
	 * <ol>
	 * <li>{@link AffineTransform#translate(double, double) translate} by the
	 * {@link INode#getPivot() pivot-point}</li>
	 * <li>{@link AffineTransform#translate(double, double) translate} by
	 * {@link INode#getTranslateX() translate-x}, {@link INode#getTranslateY()
	 * translate-y}, {@link INode#getLayoutX() layout-x},
	 * {@link INode#getLayoutY() layout-y}</li>
	 * <li>{@link AffineTransform#rotate(double) rotate} by
	 * {@link INode#getRotationAngle() rotation-angle}</li>
	 * <li>{@link AffineTransform#scale(double, double) scale} by
	 * {@link INode#getScaleX() scale-x}, {@link INode#getScaleY() scale-y}</li>
	 * <li>{@link AffineTransform#translate(double, double) translate} by the
	 * negated pivot-point</li>
	 * <li>{@link AffineTransform#concatenate(AffineTransform) concatenate} all
	 * the additional {@link INode#getTransforms() transforms}</li>
	 * </ol>
	 * </p>
	 * <p>
	 * Steps 1 and 5 are "enclosing" steps 2 to 4, so that scaling and rotation
	 * is relative to the pivot-point.
	 * </p>
	 * 
	 * @param node
	 * @return an {@link AffineTransform} to transform local coordinates of the
	 *         passed-in {@link INode} to local coordinates of its parent
	 */
	public static AffineTransform getLocalToParentTransform(INode node) {
		AffineTransform localToParentTx = new AffineTransform();

		Point pivot = node.getPivot();

		if (node.getParentNode() == null) {
			// we are root
			if (node instanceof IParent) {
				org.eclipse.swt.graphics.Point location = ((IParent) node)
						.getSwtComposite().getLocation();
				pivot = pivot.getTranslated(location.x, location.y);
			} else {
				throw new IllegalStateException(
						"no parent, but not a parent itself?!");
			}
		}

		localToParentTx.translate(node.getTranslateX() + node.getLayoutX()
				+ pivot.x, node.getTranslateY() + node.getLayoutY() + pivot.y);
		localToParentTx.rotate(node.getRotationAngle().rad());
		localToParentTx.scale(node.getScaleX(), node.getScaleY());
		localToParentTx.translate(-pivot.x, -pivot.y);

		for (AffineTransform tx : node.getTransforms()) {
			localToParentTx.concatenate(tx);
		}

		return localToParentTx;
	}

	/**
	 * <p>
	 * Transforms a {@link Point} from the local coordinate system of the
	 * passed-in {@link INode} to absolute coordinates.
	 * </p>
	 * <p>
	 * Uses: {@link INode#getLocalToAbsoluteTransform()}
	 * </p>
	 * 
	 * @param node
	 * @param localIn
	 * @param absoluteOut
	 */
	public static void localToAbsolute(INode node, Point localIn,
			Point absoluteOut) {
		AffineTransform tx = node.getLocalToAbsoluteTransform();
		absoluteOut.setLocation(tx.getTransformed(localIn));
	}

	/**
	 * <p>
	 * Transforms a {@link Point} from the local coordinate system of the
	 * passed-in {@link INode} to the coordinate system of its parent. *
	 * </p>
	 * <p>
	 * Uses: {@link INode#getLocalToParentTransform()}
	 * </p>
	 * 
	 * @param node
	 * @param localIn
	 * @param parentOut
	 */
	public static void localToParent(INode node, Point localIn, Point parentOut) {
		AffineTransform tx = node.getLocalToParentTransform();
		parentOut.setLocation(tx.getTransformed(localIn));
	}

	/**
	 * <p>
	 * Transforms the <i>parentIn</i> {@link Point} to local coordinates of the
	 * specified {@link INode}. The resulting {@link Point} is stored in
	 * <i>localOut</i>.
	 * </p>
	 * <p>
	 * Uses: {@link INode#getLocalToParentTransform()}
	 * </p>
	 * 
	 * @param node
	 * @param parentIn
	 * @param localOut
	 */
	public static void parentToLocal(INode node, Point parentIn, Point localOut) {
		AffineTransform localToParentTransform = node
				.getLocalToParentTransform();
		try {
			Point transformed = localToParentTransform
					.inverseTransform(parentIn);
			localOut.setLocation(transformed);
		} catch (NoninvertibleTransformException e) {
			// TODO: we have to assure that all transformations are invertible
		}
	}

	/**
	 * <p>
	 * Relocates the given {@link INode} by adjusting its layout-x and layout-y
	 * attributes according to the passed-in coordinates.
	 * </p>
	 * <p>
	 * Uses: {@link INode#getLayoutBounds()}
	 * </p>
	 * 
	 * @param node
	 * @param x
	 * @param y
	 */
	public static void relocate(INode node, double x, double y) {
		node.setLayoutX(x - node.getLayoutBounds().getX());
		node.setLayoutY(y - node.getLayoutBounds().getY());
	}

	public static void resizeRelocate(INode node, double x, double y,
			double width, double height) {
		// System.out.println("resizeRelocate(" + node + ", " + x + ", " + y
		// + ", " + width + ", " + height);
		node.relocate(x, y);
		node.resize(width, height);
	}

}
