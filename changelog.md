* Fixed modules potentially writing the same value to multiple ports even though they don't want to (e.g. Stack Module).
  * As a result of this fix, some timings may have changed! I tested the timings for a couple of things (Execution Module, Stack Module) where they appear to have stayed identical, but if you have a build that depends on tick-accurate operations, shut it down before updating and do tests, first. Sorry about the inconvenience!
  * Devs: this fix sadly required a small API change, in the form of one new method in the `Module` interface. If your modules extend `AbstractModule` you won't have to change anything.
* Fixed infrared modules enqueueing received packets while hosting computer is not running.
