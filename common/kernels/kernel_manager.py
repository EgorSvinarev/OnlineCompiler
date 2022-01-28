from jupyter_client import MultiKernelManager
import os

#os.chdir("/home/egor/Рабочий стол/Разное/online-compiler/IMPL/kernels/connection_files")

mngr = MultiKernelManager()
remote_id = mngr.start_kernel("python3")
# kernel_id = mngr.list_kernel_ids()[0]

print(remote_id)
while True: pass