import signal
import resource
import os
import sys

# Changing the current directory
os.chdir("/programs")

# Exception hook that handles MemoryError
def my_except_hook(exctype, value, traceback):
	if exctype == MemoryError:
		raise SystemExit("Limitation of execution: the memory limit was exceeded.")    
	else:
		sys.__excepthook__(exctype, value, traceback)


# Time limit's callback
def time_exceeded(signo, frame):
	raise SystemExit("Limitation of execution: the execution time was exceeded.")


def set_max_runtime(seconds):

	# Configurating the time limit

	soft, hard = resource.getrlimit(resource.RLIMIT_CPU)
	resource.setrlimit(resource.RLIMIT_CPU, (seconds, hard))
	signal.signal(signal.SIGXCPU, time_exceeded)

def set_max_memorylimit(maxsize):

	# Configurating the memory limit

	soft, hard = resource.getrlimit(resource.RLIMIT_AS)
	resource.setrlimit(resource.RLIMIT_AS, (maxsize, hard))  


set_max_runtime(3)
set_max_memorylimit(2 ** 30)
sys.excepthook = my_except_hook
