Drawable defaultMarker = r.getDrawable(R.drawable.icon);

// You HAVE to specify the bounds! It seems like the markers are drawn
// through Drawable.draw(Canvas) and therefore must have its bounds set
// before drawing.
defaultMarker.setBounds(0, 0, defaultMarker.getIntrinsicWidth(),
    defaultMarker.getIntrinsicHeight());

MyItemizedOverlay markers = new MyItemizedOverlay(defaultMarker);
overlays.add(markers);