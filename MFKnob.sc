MFKnob {
  var <id, <twister, <page, <color, <val = 0, <>func, rout;
  var <>buttOnFunc, <>buttOffFunc;

  *new { |id, twister, page = 0|
    ^super.newCopyArgs(id, twister, page);
  }

  color_ { |value|
    color = value;
    this.changed(\color, value);
  }

  val_ { |value|
    rout.stop;
    this.prVal_(value);
  }

  prVal_ { |value|
    if (val != value) {
      val = value;
      if (func.value(value) != false) {
        this.changed(\destinations, value);
      };
      this.changed(\val, value);
    };
  }

  fadeTo { |value = 0, dur = 1, curve = \sin, hz = 30, clock|
    clock = clock ?? TempoClock;
    rout.stop;
    rout = nil;
    if (dur == 0) {
      this.prVal_(value);
    } {
      var waittime = hz.reciprocal;
      var env = Env([val, value], [dur], curve);
      var iterations = dur * hz.floor;
      rout = {
        iterations.do { |i|
          this.prVal_(env.at((i + 1) * waittime).floor);
          waittime.wait;
        };
      }.fork(clock);
    }
  }

  buttOn {
    buttOnFunc.();
  }

  buttOff {
    buttOffFunc.();
  }
}
