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

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

@app.post("/api/detection/image")
async def detect_image(
    file: UploadFile = File(...),
    model_version: str = Form(...)
):
    try:
        logger.info(f"Received detection request for model: {model_version}")
        logger.info(f"Received file: {file.filename}, size: {file.size}")

        os.makedirs("temp", exist_ok=True)
        temp_image_path = f"temp/{uuid.uuid4()}_{file.filename}"
        with open(temp_image_path, "wb") as f:
            content = await file.read()
            f.write(content)
        logger.info(f"Saved temp image to: {temp_image_path}")

        result = yolo_service.detect(temp_image_path, model_version)
        logger.info(f"Detection completed, found {len(result['defects'])} defects")

        if os.path.exists(temp_image_path):
            os.remove(temp_image_path)

        return result
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
        logger.info(f"Received unrecognized image: {file.filename}")

        # 创建unrecognized文件夹（如果不存在）
        os.makedirs("unrecognized", exist_ok=True)
        
        # 生成唯一文件名并保存图片
        unrecognized_image_path = f"unrecognized/{uuid.uuid4()}_{file.filename}"
        with open(unrecognized_image_path, "wb") as f:
            content = await file.read()
            f.write(content)
        logger.info(f"Saved unrecognized image to: {unrecognized_image_path}")

        return {"code": 200, "message": "Image marked as unrecognized and stored successfully", "data": {}}
    except Exception as e:
        logger.error(f"Error in mark_unrecognized: {str(e)}")
        raise HTTPException(status_code=500, detail=f"Failed to store unrecognized image: {str(e)}")

if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8000)