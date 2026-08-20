from fastapi import FastAPI, UploadFile, File, Form, HTTPException
from fastapi.middleware.cors import CORSMiddleware
import uvicorn
import base64
import os
import logging
import uuid
import json
from services.yolo_service import YoloService
from services.kimi_service import KimiService

logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(name)s - %(levelname)s - %(message)s')
logger = logging.getLogger(__name__)

app = FastAPI()

yolo_service = YoloService()
kimi_service = KimiService()

# 最大上传文件大小：50MB
MAX_FILE_SIZE = 50 * 1024 * 1024

# 允许的图片 MIME 类型白名单
ALLOWED_CONTENT_TYPES = {
    "image/jpeg",
    "image/jpg",
    "image/png",
    "image/webp",
    "image/gif",
    "image/bmp",
}

# CORS 仅允许指定前端来源
app.add_middleware(
    CORSMiddleware,
    allow_origins=["http://localhost:5173", "http://localhost:8080"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


def validate_upload_file(file: UploadFile, content: bytes) -> None:
    """校验上传文件的大小与类型"""
    if len(content) > MAX_FILE_SIZE:
        raise HTTPException(status_code=413, detail="文件过大，最大支持 50MB")
    if file.content_type not in ALLOWED_CONTENT_TYPES:
        raise HTTPException(status_code=400, detail="仅支持图片文件")


def safe_filename(filename: str) -> str:
    """用 basename 净化文件名，防止路径穿越"""
    if not filename:
        return ""
    return os.path.basename(filename)


@app.post("/api/detection/image")
async def detect_image(
    file: UploadFile = File(...),
    model_version: str = Form(...)
):
    try:
        content = await file.read()
        validate_upload_file(file, content)

        filename = safe_filename(file.filename)
        logger.info(f"Received detection request for model: {model_version}")
        logger.info(f"Received file: {filename}, size: {len(content)}")

        os.makedirs("temp", exist_ok=True)
        temp_image_path = f"temp/{uuid.uuid4()}_{filename}"
        with open(temp_image_path, "wb") as f:
            f.write(content)
        logger.info(f"Saved temp image to: {temp_image_path}")

        result = yolo_service.detect(temp_image_path, model_version)
        logger.info(f"Detection completed, found {len(result['defects'])} defects")

        if os.path.exists(temp_image_path):
            os.remove(temp_image_path)

        return result
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"Error in detect_image: {str(e)}")
        raise HTTPException(status_code=500, detail=f"Detection failed: {str(e)}")

@app.post("/api/detection/analyze")
async def analyze_defects(
    defects: str = Form(...)
):
    try:
        logger.info(f"Received analysis request: {defects}")

        defect_list = json.loads(defects)
        analysis = kimi_service.analyze(defect_list)

        logger.info(f"Analysis completed successfully")
        return {"analysis": analysis}
    except Exception as e:
        logger.error(f"Error in analyze_defects: {str(e)}")
        raise HTTPException(status_code=500, detail=f"Analysis failed: {str(e)}")

@app.post("/api/detection/unrecognized")
async def mark_unrecognized(
    file: UploadFile = File(...)
):
    try:
        content = await file.read()
        validate_upload_file(file, content)

        filename = safe_filename(file.filename)
        logger.info(f"Received unrecognized image: {filename}")

        # 创建unrecognized文件夹（如果不存在）
        os.makedirs("unrecognized", exist_ok=True)

        # 生成唯一文件名并保存图片
        unrecognized_image_path = f"unrecognized/{uuid.uuid4()}_{filename}"
        with open(unrecognized_image_path, "wb") as f:
            f.write(content)
        logger.info(f"Saved unrecognized image to: {unrecognized_image_path}")

        return {"code": 200, "message": "Image marked as unrecognized and stored successfully", "data": {}}
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"Error in mark_unrecognized: {str(e)}")
        raise HTTPException(status_code=500, detail=f"Failed to store unrecognized image: {str(e)}")

if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8000)
