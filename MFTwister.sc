MFTwister {
  var <inDevice, <outDevice, <midifunc, <knobs, <channel = 0, <>outChannel = 9, <curBank = 0, <destinations;

  *new { |deviceName, portName|
    ^super.new.initMFTwister(deviceName, portName);
  }

  initMFTwister { |deviceName, portName|
    inDevice = MIDIIn.findPort(deviceName, portName);
    outDevice = MIDIOut.newByName(deviceName, portName);
    outDevice.latency_(0);
    midifunc = MIDIFunc({ |val, num, chan, src| knobs[curBank][num].val_(val); }, nil, channel, \control, inDevice.uid);
    knobs = [(0..15).collect { |i| MFKnob(i).addDependant(this) }];
    destinations = [];
  }

  update { |object, what, val|
    if (what == \val) {
      outDevice.control(channel, object.id, val);
    };
    if (what == \destinations) {
      destinations.do(_.control(outChannel, object.id, val));
    }
  }

  addDestination { |device|
    device.latency_(0);
    destinations = destinations.add(device);
  }

  free {
    midifunc.free;
  }

  at { |x = 0, y = 0, page = 0|
    ^knobs[page][y * 4 + x];
  }
}
