import requests
import os
import logging

logger = logging.getLogger(__name__)

class KimiService:
    def __init__(self):
        self.api_url = "https://api.moonshot.cn/v1/chat/completions"
        self.api_key = os.environ.get("MOONSHOT_API_KEY", "")
        self.model = "moonshot-v1-32k"

    def analyze(self, defect_types, image_info=None):
        if not self.api_key:
            logger.warning("MOONSHOT_API_KEY not set, using mock analysis")
            return self._mock_analysis(defect_types)

        defect_str = ", ".join([d.get("type", "unknown") for d in defect_types])
        confidence_str = ", ".join([f"{d.get('type', 'unknown')}: {d.get('confidence', 0):.2f}" for d in defect_types])

        prompt = f"""您是一位光伏制造专家，拥有10年以上太阳能电池板生产质量控制经验。EL图像检测系统检测到太阳能电池板存在以下缺陷：

缺陷类型及置信度：
{confidence_str}

请生成一份专业、详细的分析报告（300字以上），包含以下内容：
1. 缺陷概述：总结检测到的缺陷类型、数量和严重程度
2. 工艺分析：详细分析每个缺陷可能涉及的具体工艺步骤（如焊接、层压、丝网印刷、烧结等）
3. 根因分析：深入分析导致这些缺陷的可能原因，包括设备参数、材料质量、操作流程等方面
4. 影响评估：评估这些缺陷对电池板性能和可靠性的潜在影响
5. 优化建议：提供具体的设备调试参数和工艺优化方案，包括可操作的改进措施
6. 预防措施：提出防止类似缺陷再次发生的长期预防策略

请使用专业术语，提供详细的数据支持和具体的技术建议，确保报告具有实用性和可操作性。"""

        try:
            payload = {
                "model": self.model,
                "messages": [
                    {"role": "system", "content": "你是一位光伏制造专家，专注于太阳能电池板生产过程中的质量控制和工艺优化。"},
                    {"role": "user", "content": prompt}
                ],
                "temperature": 0.7,
                "max_tokens": 1000
            }

            response = requests.post(
                self.api_url,
                headers={
                    "Authorization": f"Bearer {self.api_key}",
                    "Content-Type": "application/json"
                },
                json=payload,
                timeout=30
            )

            if response.status_code == 200:
                result = response.json()
                return result["choices"][0]["message"]["content"]
            else:
                logger.error(f"Kimi API error: {response.status_code} - {response.text}")
                return self._mock_analysis(defect_types)

        except Exception as e:
            logger.error(f"Exception calling Kimi API: {str(e)}")
            return self._mock_analysis(defect_types)

    def _mock_analysis(self, defect_types):
        defect_str = ", ".join([d.get("type", "unknown") for d in defect_types])
        analysis = f"# 太阳能电池板EL图像缺陷分析报告\n\n"
        analysis += f"## 1. 缺陷概述\n"
        analysis += f"本次检测共识别出{len(defect_types)}处缺陷，包括：{defect_str}。整体缺陷密度较高，需要重点关注。\n\n"

        analysis += "## 2. 工艺分析\n"
        for i, defect in enumerate(defect_types, 1):
            defect_type = defect.get("type", "unknown")
            confidence = defect.get("confidence", 0)
            
            analysis += f"### {i}.{defect_type}缺陷（置信度：{confidence:.2f}）\n"
            
            if defect_type == "crack":
                analysis += "- **涉及工艺**：焊接工艺\n"
                analysis += "- **根因分析**：焊接过程中温度过高（>250℃）或压力不均（偏差>0.5N），导致电池片产生热应力和机械应力，超过硅片的断裂强度。\n"
                analysis += "- **影响评估**：裂缝会降低电池片机械强度，可能在后续层压过程中扩大，严重时导致电池片失效，影响组件发电效率约5-15%。\n"
                analysis += "- **优化建议**：调整焊接温度至220-240℃，确保温度均匀分布（温差<5℃）；检查焊接压力，保持在1.5-2.0N范围内；使用软质焊带减少应力。\n"
                analysis += "- **预防措施**：建立焊接参数定期校准机制，每8小时校准一次温度和压力；增加电池片边缘保护措施。\n\n"
            elif defect_type == "black_core":
                analysis += "- **涉及工艺**：烧结工艺\n"
                analysis += "- **根因分析**：烧结温度过高（>850℃）或时间过长（>60秒），导致硅片内部结构损伤；或硅片原材料质量不良，含有过多杂质。\n"
                analysis += "- **影响评估**：黑心缺陷会导致局部电流短路，降低电池片转换效率约3-8%，严重时可能引发热斑效应。\n"
                analysis += "- **优化建议**：优化烧结工艺参数，温度控制在820-840℃，时间控制在45-55秒；加强硅片原材料检验，确保杂质含量<0.1%。\n"
                analysis += "- **预防措施**：建立烧结炉温度分布检测系统，确保炉内温度均匀；与供应商建立原材料质量追溯体系。\n\n"
            elif defect_type == "finger":
                analysis += "- **涉及工艺**：丝网印刷工艺\n"
                analysis += "- **根因分析**：丝网印刷压力过大（>2.5N/cm²）或过小（<1.5N/cm²），印刷速度不当（>80mm/s或<40mm/s），或浆料粘度不合适（>3000cP或<1500cP）。\n"
                analysis += "- **影响评估**：手指缺陷会影响电流收集效率，降低电池片转换效率约2-6%，长期使用可能导致功率衰减加速。\n"
                analysis += "- **优化建议**：调整印刷压力至1.8-2.2N/cm²，印刷速度至50-70mm/s，浆料粘度至2000-2500cP；定期校准印刷设备精度（误差<±0.05mm）。\n"
                analysis += "- **预防措施**：建立印刷参数实时监控系统，每批次生产前进行参数校准；使用高精度丝网（目数>400）提高印刷精度。\n\n"
            elif defect_type == "short_circuit":
                analysis += "- **涉及工艺**：焊接工艺、封装工艺\n"
                analysis += "- **根因分析**：焊接过程中焊带与电池片接触不良，或绝缘材料失效；封装过程中EVA胶膜融化不完全，导致局部短路。\n"
                analysis += "- **影响评估**：短路缺陷会导致局部电流异常，可能引发热斑效应，严重时损坏整个组件，影响系统安全性。\n"
                analysis += "- **优化建议**：检查焊接设备的焊接质量，确保焊带与电池片良好接触（接触电阻<5mΩ）；确保EVA胶膜完全融化（层压温度145-155℃，时间15-20分钟）。\n"
                analysis += "- **预防措施**：建立焊接质量检测系统，每批次抽检10%的组件；优化封装工艺参数，确保EVA胶膜充分交联。\n\n"
            elif defect_type == "thick_line":
                analysis += "- **涉及工艺**：丝网印刷工艺\n"
                analysis += "- **根因分析**：丝网印刷时浆料过多，或印刷速度过慢，导致金属浆料在电池片表面堆积。\n"
                analysis += "- **影响评估**：粗线缺陷会增加遮光面积，降低电池片转换效率约1-3%，同时可能影响后续焊接质量。\n"
                analysis += "- **优化建议**：调整浆料的浓度和用量（固含量30-35%）；优化印刷速度至50-70mm/s，确保浆料均匀分布。\n"
                analysis += "- **预防措施**：建立浆料粘度定期检测机制，每4小时检测一次；使用自动刮墨系统控制浆料用量。\n\n"
            else:
                analysis += "- **涉及工艺**：需要进一步分析\n"
                analysis += "- **根因分析**：需要进一步检查生产过程中的各个环节。\n"
                analysis += "- **影响评估**：具体影响需要根据缺陷类型和严重程度确定。\n"
                analysis += "- **优化建议**：进行详细检测以确定具体原因，针对性制定改进措施。\n"
                analysis += "- **预防措施**：加强生产过程监控，建立缺陷数据库，分析缺陷发生规律。\n\n"

        analysis += "## 3. 结论与建议\n"
        analysis += "本次检测结果表明生产过程中存在工艺参数控制不稳定的问题，建议重点关注相关工艺环节的优化。通过调整上述参数和加强设备维护，可以有效减少此类缺陷的产生，提升太阳能电池板的产品质量和可靠性。\n"
        analysis += "建议在未来生产中建立实时质量监控系统，对关键工艺参数进行在线监测，及时发现和解决问题，确保产品质量的稳定性。" 
        return analysis
