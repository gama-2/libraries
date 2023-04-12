/*******************************************************************************************************
 *
 * ImageDisplaySurface.java, in ummisco.gaml.extensions.image, is part of the source code of the GAMA modeling and
 * simulation platform (v.1.9.0).
 *
 * (c) 2007-2023 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.extension.images;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.RenderedImage;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.util.Collection;
import java.util.Collections;

import javax.imageio.ImageIO;

import org.locationtech.jts.geom.Envelope;

import gama.core.annotations.common.interfaces.IKeyword;
import gama.core.annotations.precompiler.GamlAnnotations.display;
import gama.core.annotations.precompiler.GamlAnnotations.doc;
import gama.core.common.interfaces.IDisplaySurface;
import gama.core.common.interfaces.IGraphics;
import gama.core.common.interfaces.ILayer;
import gama.core.common.interfaces.ILayerManager;
import gama.core.common.preferences.GamaPreferences;
import gama.core.metamodel.agent.IAgent;
import gama.core.metamodel.shape.GamaPoint;
import gama.core.metamodel.shape.IShape;
import gama.core.outputs.LayeredDisplayData;
import gama.core.outputs.LayeredDisplayOutput;
import gama.core.outputs.LayeredDisplayData.Changes;
import gama.core.outputs.display.AWTDisplayGraphics;
import gama.core.outputs.display.LayerManager;
import gama.core.outputs.layers.IEventLayerListener;
import gama.core.runtime.GAMA;
import gama.core.runtime.IScope.IGraphicsScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.GamaListFactory;
import gama.core.util.IList;
import gaml.core.operators.Files;

/**
 * The Class ImageDisplaySurface.
 */

/**
 * The Class ImageDisplaySurface.
 */
@display (
		value = IKeyword.IMAGE)
@doc ("A display used to save the graphical representations of agents into image files")
public class ImageDisplaySurface implements IDisplaySurface {

	/** The output. */
	private final LayeredDisplayOutput output;

	/** The buff image. */
	// private final boolean needsUpdate = true;
	private GamaImage buffImage = null;

	/** The g 2. */
	private Graphics2D g2 = null;

	/** The height. */
	private int width = 500, height = 500;

	/** The display graphics. */
	private IGraphics displayGraphics;

	/** The manager. */
	ILayerManager manager;

	/** The snapshot folder. */
	public static String snapshotFolder = "/tmp/";

	/** The scope. */
	protected IGraphicsScope scope;

	/** The data. */
	private final LayeredDisplayData data;

	/**
	 * Instantiates a new image display surface.
	 *
	 * @param args
	 *            the args
	 */
	public ImageDisplaySurface(final Object... args) {
		output = (LayeredDisplayOutput) args[0];
		data = output.getData();

	}

	/**
	 * @see gama.core.common.interfaces.IDisplaySurface#initialize(double, double, gama.core.outputs.IDisplayOutput)
	 */
	@Override
	public void outputReloaded() {
		this.scope = output.getScope().copyForGraphics("in image surface of " + output.getName());
		if (!GamaPreferences.Runtime.ERRORS_IN_DISPLAYS.getValue()) { scope.disableErrorReporting(); }
		if (manager == null) {
			manager = new LayerManager(this, output);
		} else {
			manager.outputChanged();
		}

	}

	@Override
	public IGraphicsScope getScope() { return scope; }

	/**
	 * Save this surface into an image passed as a parameter
	 *
	 * @param actionScope
	 * @param image
	 */
	public void save(final RenderedImage image) {
		try {
			Files.newFolder(scope, snapshotFolder);
		} catch (final GamaRuntimeException e1) {
			e1.addContext("Impossible to create folder " + snapshotFolder);
			GAMA.reportError(scope, e1, false);
			e1.printStackTrace();
			return;
		}
		final String file =
				snapshotFolder + "/" + GAMA.getModel().getName() + "_display_" + scope.getClock().getCycle() + ".png";
		// DataOutputStream os = null;
		try (DataOutputStream os = new DataOutputStream(new FileOutputStream(file))) {
			ImageIO.write(image, "png", os);
		} catch (final java.io.IOException ex) {
			final GamaRuntimeException e = GamaRuntimeException.create(ex, scope);
			e.addContext("Unable to create output stream for snapshot image");
			GAMA.reportError(getScope(), e, false);
		}
	}

	@Override
	public ILayerManager getManager() { return manager; }

	/**
	 * Resize image.
	 *
	 * @param newWidth
	 *            the new width
	 * @param newHeight
	 *            the new height
	 * @param force
	 *            the force
	 * @return true, if successful
	 */
	public boolean resizeImage(final int newWidth, final int newHeight, final boolean force) {
		if (!force && width == newWidth && height == newHeight) return false;
		this.width = newWidth;
		this.height = newHeight;
		final Image copy = buffImage;
		createBuffImage();
		if (getScope() != null && getScope().isPaused()) {
			updateDisplay(true);
		} else {
			g2.drawImage(copy, 0, 0, newWidth, newHeight, null);
		}
		if (copy != null) { copy.flush(); }
		return true;
	}

	@Override
	public void updateDisplay(final boolean force) {
		// if ( needsUpdate || force ) {
		drawAllDisplays();
		// }
	}

	/**
	 * Draw all displays.
	 */
	private void drawAllDisplays() {
		if (displayGraphics == null) return;
		displayGraphics.fillBackground(data.getBackgroundColor());
		manager.drawLayersOn(displayGraphics);
	}

	/**
	 * Creates the buff image.
	 */
	private void createBuffImage() {
		buffImage = GamaImage.ofDimensions(width, height);
		g2 = (Graphics2D) buffImage.getGraphics();
		displayGraphics = new AWTDisplayGraphics((Graphics2D) buffImage.getGraphics());
		((AWTDisplayGraphics) displayGraphics).setGraphics2D((Graphics2D) buffImage.getGraphics());
		((AWTDisplayGraphics) displayGraphics).setUntranslatedGraphics2D((Graphics2D) buffImage.getGraphics());
		displayGraphics.setDisplaySurface(this);
	}

	/**
	 * Paint.
	 */
	private void paint() {
		if (buffImage == null) { createBuffImage(); }
		drawAllDisplays();

	}

	@Override
	public void dispose() {
		if (g2 != null) { g2.dispose(); }
		if (manager != null) { manager.dispose(); }
		GAMA.releaseScope(scope);
	}

	@Override
	public GamaImage getImage(final int w, final int h) {
		paint();
		return ImageHelper.resize(buffImage, gaml.extension.images.ImageHelper.Mode.FIT_EXACT, w, h);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see msi.gama.gui.graphics.IDisplaySurface#zoomIn(msi.gama.gui.views. IGamaView)
	 */
	@Override
	public void zoomIn() {

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see msi.gama.gui.graphics.IDisplaySurface#zoomOut(msi.gama.gui.views. IGamaView)
	 */
	@Override
	public void zoomOut() {

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see msi.gama.gui.graphics.IDisplaySurface#zoomFit(msi.gama.gui.views. IGamaView)
	 */
	@Override
	public void zoomFit() {

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see msi.gama.gui.graphics.IDisplaySurface#fireSelectionChanged(java.lang. Object)
	 */
	// @Override
	// public void fireSelectionChanged(final Object a) {
	//
	//
	// }

	/*
	 * (non-Javadoc)
	 *
	 * @see msi.gama.gui.graphics.IDisplaySurface#focusOn(msi.gama.util.GamaGeometry, msi.gama.gui.displays.IDisplay)
	 */
	@Override
	public void focusOn(final IShape geometry) {

	}

	//
	// @Override
	// public void canBeUpdated(final boolean ok) {
	// needsUpdate = ok;
	// }

	/**
	 * @see gama.core.common.interfaces.IDisplaySurface#getWidth()
	 */
	@Override
	public int getWidth() { return width; }

	/**
	 * @see gama.core.common.interfaces.IDisplaySurface#getHeight()
	 */
	@Override
	public int getHeight() { return height; }

	@Override
	public void addListener(final IEventLayerListener e) {}

	@Override
	public double getEnvWidth() { return data.getEnvWidth(); }

	@Override
	public double getEnvHeight() { return data.getEnvHeight(); }

	@Override
	public double getDisplayWidth() { return width; }

	@Override
	public double getDisplayHeight() { return this.getHeight(); }

	// @Override
	// public void setZoomListener(final IZoomListener listener) {}
	//
	/**
	 * Method getModelCoordinates()
	 *
	 * @see gama.core.common.interfaces.IDisplaySurface#getModelCoordinates()
	 */
	@Override
	public GamaPoint getModelCoordinates() { return null; }

	/**
	 * Method getZoomLevel()
	 *
	 * @see gama.core.common.interfaces.IDisplaySurface#getZoomLevel()
	 */
	@Override
	public double getZoomLevel() { return 1.0; }

	/**
	 * Method setSize()
	 *
	 * @see gama.core.common.interfaces.IDisplaySurface#setSize(int, int)
	 */
	@Override
	public void setSize(final int x, final int y) {
		resizeImage(x, y, false);
	}

	/**
	 * Method removeMouseListener()
	 *
	 * @see gama.core.common.interfaces.IDisplaySurface#removeMouseListener(java.awt.event.MouseListener)
	 */
	@Override
	public void removeListener(final IEventLayerListener e) {}

	@Override
	public Collection<IEventLayerListener> getLayerListeners() { return Collections.EMPTY_LIST; }

	@Override
	public GamaPoint getModelCoordinatesFrom(final int xOnScreen, final int yOnScreen, final Point sizeInPixels,
			final Point positionInPixels) {
		final double xScale = sizeInPixels.x / getEnvWidth();
		final double yScale = sizeInPixels.y / getEnvHeight();
		final int xInDisplay = xOnScreen - positionInPixels.x;
		final int yInDisplay = yOnScreen - positionInPixels.y;
		final double xInModel = xInDisplay / xScale;
		final double yInModel = yInDisplay / yScale;
		return new GamaPoint(xInModel, yInModel);
	}

	@Override
	public IList<IAgent> selectAgent(final int xc, final int yc) {
		return GamaListFactory.EMPTY_LIST;
		// final IList<IAgent> result = GamaListFactory.create(Types.AGENT);
		// final List<ILayer> layers = getManager().getLayersIntersecting(xc,
		// yc);
		// for (final ILayer layer : layers) {
		// if (layer.isSelectable()) {
		// final Set<IAgent> agents = layer.collectAgentsAt(xc, yc, this);
		// if (!agents.isEmpty()) {
		// result.addAll(agents);
		// }
		// }
		// }
		// return result;
	}

	/**
	 * Method getOutput()
	 *
	 * @see gama.core.common.interfaces.IDisplaySurface#getOutput()
	 */
	@Override
	public LayeredDisplayOutput getOutput() { return output; }

	/**
	 * Method waitForUpdateAndRun()
	 *
	 * @see gama.core.common.interfaces.IDisplaySurface#waitForUpdateAndRun(java.lang.Runnable)
	 */
	@Override
	public void runAndUpdate(final Runnable r) {
		r.run();
	}

	/**
	 * Method getData()
	 *
	 * @see gama.core.common.interfaces.IDisplaySurface#getData()
	 */
	@Override
	public LayeredDisplayData getData() { return data; }

	/**
	 * Method setSWTMenuManager()
	 *
	 * @see msi.gama.common.interfaces.IDisplaySurface#setSWTMenuManager(java.lang.Object)
	 */
	// @Override
	// public void setSWTMenuManager(final Object displaySurfaceMenu) {
	// }

	/**
	 * Method layersChanged()
	 *
	 * @see gama.core.common.interfaces.IDisplaySurface#layersChanged()
	 */
	@Override
	public void layersChanged() {}

	/**
	 * Method changed()
	 *
	 * @see gama.core.outputs.LayeredDisplayData.DisplayDataListener#changed(gama.core.outputs.LayeredDisplayData.Changes,
	 *      boolean)
	 */
	@Override
	public void changed(final Changes property, final Object value) {}

	/**
	 * Method getVisibleRegionForLayer()
	 *
	 * @see gama.core.common.interfaces.IDisplaySurface#getVisibleRegionForLayer(gama.core.common.interfaces.ILayer)
	 */
	@Override
	public Envelope getVisibleRegionForLayer(final ILayer currentLayer) {
		return null;
	}

	/**
	 * Method getFPS()
	 *
	 * @see gama.core.common.interfaces.IDisplaySurface#getFPS()
	 */
	@Override
	public int getFPS() { return 0; }

	@Override
	public boolean isDisposed() { return false; }

	@Override
	public void getModelCoordinatesInfo(final StringBuilder sb) {}

	@Override
	public void dispatchKeyEvent(final char character) {}

	@Override
	public void dispatchSpecialKeyEvent(final int e) {}

	/**
	 * Dispatch mouse event.
	 *
	 * @param swtEventType
	 *            the swt event type
	 */
	@Override
	public void dispatchMouseEvent(final int swtEventType, final int x, final int y) {}

	@Override
	public void setMousePosition(final int x, final int y) {}

	@Override
	public void draggedTo(final int x, final int y) {}

	@Override
	public void selectAgentsAroundMouse() {}

	@Override
	public void setMenuManager(final Object displaySurfaceMenu) {}

	@Override
	public boolean isVisible() { return true; }

	@Override
	public IGraphics getIGraphics() { return displayGraphics; }

	@Override
	public Rectangle getBoundsForRobotSnapshot() { return new Rectangle(0, 0, buffImage.getWidth(), buffImage.getHeight()); }

	@Override
	public boolean shouldWaitToBecomeRendered() {
		return false;
	}

}