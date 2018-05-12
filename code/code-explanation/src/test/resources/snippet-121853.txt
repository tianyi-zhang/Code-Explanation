void onDraw() {
  ...
  synchronized (stick) {
    stick.notify();
  }
} // end onDraw()