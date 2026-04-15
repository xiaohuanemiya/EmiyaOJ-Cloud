FROM criyle/go-judge:latest

USER root

# 安装编译环境: GCC/G++, OpenJDK, Python3, Go
RUN apt-get update && apt-get install -y --no-install-recommends \
    build-essential \
    && rm -rf /var/lib/apt/lists/*

# 验证安装
RUN gcc --version && g++ --version
