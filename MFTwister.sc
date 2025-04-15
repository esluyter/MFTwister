MFTwister {
  var <inDevice, <outDevice, <mfChannels, <midiFuncs, <knobs, <>outChannel = 9, <curPage = 0, <destinations;

  *new { |deviceName = "Midi Fighter Twister", portName = "Midi Fighter Twister", numPages = 1|
    ^super.new.initMFTwister(deviceName, portName, numPages);
  }

  initMFTwister { |deviceName, portName, numPages|
    inDevice = MIDIIn.findPort(deviceName, portName);
    outDevice = MIDIOut.newByName(deviceName, portName);
    outDevice.latency_(0);
    mfChannels = (
      knob: 0,
      butt: 1,
      animation: 2,
      sys: 3
    );
    midiFuncs = (
      knob: MIDIFunc({ |val, num|
        knobs[curPage][num].val_(val);
      }, nil, mfChannels[\knob], \control, inDevice.uid),
      butt: MIDIFunc({ |val, num|
        var knob = knobs[curPage][num];
        if (val == 0) {
          knob.buttOff;
          this.setColor(num, knob.color);
        } {
          knob.buttOn;
          this.setColor(num, if (knob.color.class == Color) { knob.color.complementary } { 127 });
        }
      }, nil, mfChannels[\butt], \control, inDevice.uid),
    );
    knobs = { |page| (0..63).collect { |i| MFKnob(i, this, page).addDependant(this) } } ! numPages;
    destinations = [];
  }

  update { |object, what, val|
    if (what == \val) {
      outDevice.control(mfChannels[\knob], object.id, val);
    };
    if (what == \destinations) {
      destinations.do(_.control(outChannel, object.id, val));
    };
    if (what == \color) {
      if (object.page == curPage) {
        this.setColor(object.id, object.color);
      };
    };
  }

  addDestination { |device|
    device.latency_(0);
    destinations = destinations.add(device);
  }

  free {
    knobs.do { |arr|
      arr.do(_.release);
    };
    midiFuncs.do(_.free);
  }

  at { |x = 0, y = nil, page = 0|
    ^if (y.notNil) { knobs[page][y * 4 + x] } { knobs[page][x] };
  }

  copySeries { |first, second, last|
    ^knobs[curPage].copySeries(first, second, last);
  }

  setColor { |num = 0, color|
    // color 0-127
    // 0 => unpressed color, 127 => pressed color
    // 1-126 => custom color
    if (color == nil) { color = 0 };
    if (color.class == Color) {
      var hsv = color.asHSV;
      color = ((0.66 - hsv[0]) % 1).linlin(0, 1, 1, 126);
      outDevice.control(mfChannels[\animation], num, hsv[2].linlin(0, 1, 17, 47));
    };
    outDevice.control(mfChannels[\butt], num, color)
  }
}