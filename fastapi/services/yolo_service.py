import cv2
import numpy as np
import os
import base64
import logging

class YoloService:
    def __init__(self):
        self.model_path = os.path.join(os.path.dirname(__file__), "models", "yolov11", "best.pt")
        if not os.path.exists(self.model_path):
            logging.error(f"Model file not found: {self.model_path}")
            raise FileNotFoundError(f"Model file not found: {self.model_path}")
        self.model = self.load_model()
        logging.info("YOLO model loaded successfully")
    
    def load_model(self):
        try:
            from ultralytics import YOLO
            model = YOLO(self.model_path)
            return model
        except ImportError as e:
            logging.error(f"Failed to import YOLO: {e}")
            raise
        except Exception as e:
            logging.error(f"Failed to load model: {e}")
            raise
    
    def detect(self, image_path, model_version):
        try:
            results = self.model.predict(image_path, conf=0.5)
            
            defects = []
            for result in results:
                for box in result.boxes:
                    cls = int(box.cls[0])
                    conf = float(box.conf[0])
                    bbox = box.xyxy[0].tolist()
                    
                    defect_type = self.get_defect_type(cls)
                    defects.append({
                        "type": defect_type,
                        "confidence": conf,
                        "bbox": [int(coord) for coord in bbox]
                    })
            
            result_image = self.draw_boxes(image_path, results)
            result_image_base64 = self.image_to_base64(result_image)
            
            return {
                "defects": defects,
                "result_image": result_image_base64
            }
        except Exception as e:
            logging.error(f"Detection failed: {e}")
            raise
    
    def get_defect_type(self, class_id):
        class_map = {
            0: "crack",
            1: "finger",
            2: "black_core",
            3: "thick_line",
            4: "star_crack",
            5: "corner",
            6: "fragment",
            7: "scratch",
            8: "horizontal_dislocation",
            9: "vertical_dislocation",
            10: "printing_error",
            11: "short_circuit"
        }
        return class_map.get(class_id, "unknown")
    
    def draw_boxes(self, image_path, results):
        image = cv2.imread(image_path)
        for result in results:
            for box in result.boxes:
                x1, y1, x2, y2 = box.xyxy[0].tolist()
                cls = int(box.cls[0])
                conf = float(box.conf[0])
                
                color = self.get_color(cls)
                cv2.rectangle(image, (int(x1), int(y1)), (int(x2), int(y2)), color, 2)
                label = f"{self.get_defect_type(cls)}: {conf:.2f}"
                cv2.putText(image, label, (int(x1), int(y1) - 10),
                           cv2.FONT_HERSHEY_SIMPLEX, 0.5, color, 2)
        return image
    
    def get_color(self, class_id):
        colors = {
            0: (0, 0, 255),     # crack - red
            1: (0, 255, 0),     # finger - green
            2: (255, 0, 0),     # black_core - blue
            3: (0, 255, 255),   # thick_line - yellow
            4: (255, 0, 255),   # star_crack - magenta
            5: (128, 0, 128),   # corner - purple
            6: (0, 128, 0),     # fragment - dark green
            7: (128, 128, 0),   # scratch - olive
            8: (255, 128, 0),   # horizontal_dislocation - orange
            9: (128, 0, 255),   # vertical_dislocation - violet
            10: (0, 128, 128),  # printing_error - teal
            11: (255, 128, 128) # short_circuit - light red
        }
        return colors.get(class_id, (255, 255, 255))
    
    def image_to_base64(self, image):
        _, buffer = cv2.imencode('.jpg', image)
        return base64.b64encode(buffer).decode('utf-8')