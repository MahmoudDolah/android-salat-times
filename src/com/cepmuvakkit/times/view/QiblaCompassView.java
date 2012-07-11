package com.cepmuvakkit.times.view;

import java.text.DecimalFormat;
import java.util.GregorianCalendar;
import com.cepmuvakkit.times.posAlgo.AstroLib;
import com.cepmuvakkit.times.posAlgo.EarthHeading;
import com.cepmuvakkit.times.posAlgo.Horizontal;
import com.cepmuvakkit.times.posAlgo.SunMoonPosition;
import com.cepmuvakkit.times.R;
import android.content.Context;
import android.graphics.*;
import android.view.*;
import android.util.AttributeSet;
import android.content.res.Resources;

public class QiblaCompassView extends View {
	private Paint dashedPaint;
	private int px, py; // Center of Compass (px,py)
	private int Radius; // Radius of Compass dial
	private int r; // Radius of Sun and Moon
	private String northString, eastString, southString, westString;
	private int textHeight;
	private DashPathEffect dashPath;
	private float bearing;
	private Resources rsc;
	private Horizontal sunPosition, moonPosition;
	private EarthHeading qiblaInfo;
	private SunMoonPosition sunMoonPosition;
	private double longitude, latitude, altitude = 0;;
	private Paint textPaint;
	private DecimalFormat TwoDigitFormat;

	public QiblaCompassView(Context context) {
		super(context);
		// initAstronomicParameters();
		initCompassView();
	}

	public QiblaCompassView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initCompassView();
	}

	public QiblaCompassView(Context context, AttributeSet ats, int defaultStyle) {
		super(context, ats, defaultStyle);
		initCompassView();
	}

	private void initAstronomicParameters() {
		// longitude = 32.85;
		// latitude = 39.95;
		GregorianCalendar c = new GregorianCalendar();
		double jd = AstroLib.calculateJulianDay(c);

		double ΔT = 0;
		sunMoonPosition = new SunMoonPosition(jd, latitude, longitude,
				altitude, ΔT);
		sunPosition = sunMoonPosition.getSunPosition();
		moonPosition = sunMoonPosition.getMoonPosition();

	}

	public void initCompassView() {
		setFocusable(true);
		initAstronomicParameters();
		rsc = this.getResources();
		northString = rsc.getString(R.string.cardinal_north);
		eastString = rsc.getString(R.string.cardinal_east);
		southString = rsc.getString(R.string.cardinal_south);
		westString = rsc.getString(R.string.cardinal_west);

		dashPath = new DashPathEffect(new float[] { 2, 5 }, 1);
		dashedPaint = new Paint(Paint.FAKE_BOLD_TEXT_FLAG);
		dashedPaint.setPathEffect(dashPath);
		dashedPaint.setStrokeWidth(2);
		dashedPaint.setPathEffect(dashPath);
		dashedPaint.setColor(rsc.getColor(R.color.marker_color));

		textPaint = new Paint(Paint.FAKE_BOLD_TEXT_FLAG);
		textPaint.setColor(rsc.getColor(R.color.text_color));
		textPaint.setTextSize(20);
		TwoDigitFormat = new DecimalFormat("#0.00°");

	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// The compass is a circle that fills as much space as possible.
		// Set the measured dimensions by figuring out the shortest boundary,
		// height or width.
		int measuredWidth = measure(widthMeasureSpec);
		int measuredHeight = measure(heightMeasureSpec);

		// int d = Math.min(measuredWidth, measuredHeight);

		setMeasuredDimension(measuredWidth, measuredHeight);
	}

	private int measure(int measureSpec) {
		int result = 0;

		// Decode the measurement specifications.
		int specMode = MeasureSpec.getMode(measureSpec);
		int specSize = MeasureSpec.getSize(measureSpec);

		if (specMode == MeasureSpec.UNSPECIFIED) {
			// Return a default size of 200 if no bounds are specified.
			result = 600;
		} else {
			// As you want to fill the available space
			// always return the full available bounds.
			result = specSize;
		}
		return result;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		this.Radius = Math.min(px, py);
		this.r = Radius / 10; // Sun Moon radius;
		// over here
		qiblaInfo = sunMoonPosition.getDestinationHeading();
		textPaint.setTextAlign(Paint.Align.CENTER);
		textPaint.setAntiAlias(true);
		textPaint.setColor(Color.CYAN);
		canvas.drawText(" " + TwoDigitFormat.format(latitude) + " "
				+ TwoDigitFormat.format(longitude), px, 40, textPaint);
		
		textPaint.setColor(Color.GREEN);
		canvas.drawText("Qibla Angle : "
				+ TwoDigitFormat.format(qiblaInfo.getHeading()), px,
				2 * py - 20, textPaint);
		textPaint.setTextAlign(Paint.Align.LEFT);
		textPaint.setColor(Color.WHITE);
		canvas.rotate(-bearing, px, py);// Attach and Detach capability lies
		canvas.save();
		drawDial(canvas);
		canvas.save();
		drawQibla(canvas);
		canvas.save();
		drawTrueNorthArrow(canvas, bearing);
		canvas.save();
		drawMoon(canvas);
		canvas.save();
		drawSun(canvas);
		canvas.save();

	}

	public void drawTrueNorthArrow(Canvas canvas, float drawnAngle)

	{
		Path mPath = new Path();
		Paint trueNorthArrowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		trueNorthArrowPaint.setAntiAlias(true);
		trueNorthArrowPaint.setColor(Color.RED);
		trueNorthArrowPaint.setStyle(Paint.Style.FILL);
		trueNorthArrowPaint.setAlpha(100);
		int r = Radius / 12;
		// Construct a wedge-shaped path
		mPath.moveTo(px, py - px);
		mPath.lineTo(px - r, py);
		mPath.lineTo(px, py + r);
		mPath.lineTo(px + r, py);
		mPath.addCircle(px, py, r, Path.Direction.CCW);
		mPath.close();
		canvas.drawPath(mPath, trueNorthArrowPaint);
		dashedPaint.setColor(Color.RED);
		canvas.drawLine(px, py - px, px, py + Radius, dashedPaint);
		canvas.drawCircle(px, py, 5, dashedPaint);
		canvas.restore();
	}

	public void drawDial(Canvas canvas) {
		// over here
		Paint markerPaint, circlePaint;
		circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		circlePaint.setColor(rsc.getColor(R.color.background_color));
		circlePaint.setStrokeWidth(1);
		circlePaint.setStyle(Paint.Style.STROKE); // Sadece Cember ciziyor.

		textHeight = (int) textPaint.measureText("yY");
		markerPaint = new Paint(Paint.FAKE_BOLD_TEXT_FLAG);
		markerPaint.setColor(rsc.getColor(R.color.marker_color));
		// Draw the background
		canvas.drawCircle(px, py, Radius, circlePaint);
		canvas.drawCircle(px, py, Radius - 20, circlePaint);
		// Rotate our perspective so that the �top� is
		// facing the current bearing.

		int textWidth = (int) textPaint.measureText("W");
		int cardinalX = px - textWidth / 2;
		int cardinalY = py - Radius + textHeight;

		// Draw the marker every 15 degrees and text every 45.
		for (int i = 0; i < 24; i++) {
			// Draw a marker.
			canvas.drawLine(px, py - Radius, px, py - Radius + 10, markerPaint);
			canvas.save();
			canvas.translate(0, textHeight);
			// Draw the cardinal points
			if (i % 6 == 0) {
				String dirString = "";
				switch (i) {
				case (0): {
					dirString = northString;
					break;
				}
				case (6):
					dirString = eastString;
				break;
				case (12):
					dirString = southString;
				break;
				case (18):
					dirString = westString;
				break;
				}
				canvas.drawText(dirString, cardinalX, cardinalY, textPaint);
			}

			else if (i % 3 == 0) {
				// Draw the text every alternate 45deg
				String angle = String.valueOf(i * 15);
				float angleTextWidth = textPaint.measureText(angle);
				int angleTextX = (int) (px - angleTextWidth / 2);
				int angleTextY = py - Radius + textHeight;
				canvas.drawText(angle, angleTextX, angleTextY, textPaint);
			}
			canvas.restore();

			canvas.rotate(15, px, py);
		}

	}

	public void drawSun(Canvas canvas) {
		Paint sunPaint;
		sunPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		sunPaint.setColor(Color.YELLOW);
		sunPaint.setStyle(Paint.Style.FILL_AND_STROKE);
		// Horizontal sunPosition = new Horizontal(225, 45);

		if (sunPosition.getElevation() > -10) {
			canvas.rotate((float) sunPosition.getAzimuth() - 360, px, py);
			sunPaint.setPathEffect(dashPath);

			int ry = (int) (((90 - sunPosition.getElevation()) / 90) * Radius);
			canvas.drawCircle(px, py - ry, r, sunPaint);
			dashedPaint.setColor(Color.YELLOW);
			canvas.drawLine(px, py - Radius, px, py + Radius, dashedPaint);
			sunPaint.setPathEffect(null);
			canvas.restore();
		}

	}

	public void drawMoon(Canvas canvas)

	{
		Paint moonPaint, moonPaintB, moonPaintO, moonPaintD;
		moonPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		moonPaint.setColor(Color.WHITE);
		moonPaint.setStyle(Paint.Style.FILL_AND_STROKE);
		moonPaintB = new Paint(Paint.ANTI_ALIAS_FLAG);// moon Paint Black
		moonPaintB.setColor(Color.BLACK);
		moonPaintB.setStyle(Paint.Style.FILL_AND_STROKE);
		moonPaintO = new Paint(Paint.ANTI_ALIAS_FLAG);// moon Paint for Oval
		moonPaintO.setColor(Color.WHITE);
		moonPaintO.setStyle(Paint.Style.FILL_AND_STROKE);
		moonPaintD = new Paint(Paint.ANTI_ALIAS_FLAG);// moon Paint for Diameter
		// draw
		moonPaintD.setColor(Color.GRAY);
		moonPaintD.setStyle(Paint.Style.STROKE);
		double moonPhase = sunMoonPosition.getMoonPhase();
		if (moonPosition.getElevation() > -5) {
			canvas.rotate((float) moonPosition.getAzimuth() - 360, px, py);
			int eOffset = (int) ((moonPosition.getElevation() / 90) * Radius);
			// elevation Offset 0 for 0 degree; r for 90 degree
			RectF moonRect = new RectF(px - r, py + eOffset - Radius - r, px
					+ r, py + eOffset - Radius + r);
			canvas.drawArc(moonRect, 90, 180, false, moonPaint);
			canvas.drawArc(moonRect, 270, 180, false, moonPaintB);
			int arcWidth = (int) ((moonPhase - 0.5) * (4 * r));
			moonPaintO.setColor(arcWidth < 0 ? Color.BLACK : Color.WHITE);
			RectF moonOval = new RectF(px - Math.abs(arcWidth) / 2, py
					+ eOffset - Radius - r, px + Math.abs(arcWidth) / 2, py
					+ eOffset - Radius + r);
			canvas.drawArc(moonOval, 0, 360, false, moonPaintO);
			canvas.drawArc(moonRect, 0, 360, false, moonPaintD);
			moonPaintD.setPathEffect(dashPath);
			canvas.drawLine(px, py - Radius, px, py + Radius, moonPaintD);
			moonPaintD.setPathEffect(null);
			canvas.restore();

		}

	}

	public void drawQibla(Canvas canvas) {
		Paint qiblaPaint;

		canvas.rotate((float) qiblaInfo.getHeading() - 360, px, py);
		qiblaPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		qiblaPaint.setColor(Color.GREEN);
		qiblaPaint.setStyle(Paint.Style.FILL_AND_STROKE);
		qiblaPaint.setPathEffect(dashPath);
		qiblaPaint.setStrokeWidth(5.5f);

		canvas.drawLine(px, py - Radius, px, py + Radius, qiblaPaint);
		qiblaPaint.setPathEffect(null);
		Bitmap bmp = BitmapFactory.decodeResource(getResources(),
				R.drawable.kabah);
		canvas.drawBitmap(bmp, px - bmp.getWidth() / 2, py - Radius
				- bmp.getHeight() / 2, qiblaPaint);
		canvas.restore();

	}

	public void setBearing(float _bearing) {
		bearing = _bearing;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public void setScreenResolution(int widthPixels, int heightPixels) {
		this.px = widthPixels / 2;
		this.py = heightPixels / 2;
	}
}