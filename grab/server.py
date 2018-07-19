import sys, Ice, signal

from orginfo import OrgServiceI

with Ice.initialize(sys.argv) as communicator:
	#
    # Install a signal handler to shutdown the communicator on Ctrl-C
    #
    signal.signal(signal.SIGINT, lambda signum, frame: communicator.shutdown())

    #
    # The communicator initialization removes all Ice-related arguments from argv
    #
    if len(sys.argv) > 1:
    	print(sys.argv[0] + ": too many arguments")
    	sys.exit(1)

    properties = communicator.getProperties()
    adapter = communicator.createObjectAdapter("OrgService")
    id = Ice.stringToIdentity(properties.getProperty("Identity"))
    adapter.add(OrgServiceI(), id)

    adapter.active()

    #
    communicator.waitForShutdown()