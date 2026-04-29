FROM criyle/go-judge:latest

USER root

# 临时替换为清华源（Debian Trixie）
RUN sed -i 's|http://deb.debian.org|http://mirrors.tuna.tsinghua.edu.cn|g' /etc/apt/sources.list.d/debian.sources

# 安装编译环境: GCC/G++, OpenJDK 21, Python3, Go
RUN apt-get update && apt-get install -y --no-install-recommends \
    build-essential \
    openjdk-21-jdk \
    python3 \
    golang \
    && rm -rf /var/lib/apt/lists/*

# 验证安装
RUN gcc --version && g++ --version && javac -version && python3 --version && go version