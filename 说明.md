## minIO配置桶匿名可读
# 2. 进入 MinIO 容器内部（容器已预装 mc 工具）
docker exec -it minio-server sh

# 3. 配置别名（在容器内执行）
mc alias set local http://localhost:9000 minioadmin minioadmin

# 4. 设置 ai-notes 桶为匿名只读（可下载，不可删除）
mc anonymous set download local/ai-notes

# 5. 退出容器
exit